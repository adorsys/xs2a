/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.aspsp.aspspmockserver.service;

import de.adorsys.aspsp.aspspmockserver.config.rest.consent.PisConsentRemoteUrls;
import de.adorsys.aspsp.aspspmockserver.repository.PaymentRepository;
import de.adorsys.aspsp.aspspmockserver.service.mapper.PaymentMapper;
import de.adorsys.aspsp.xs2a.consent.api.pis.proto.PisConsentResponse;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountBalance;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountDetails;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiBalances;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiAmount;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiConsentStatus;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPeriodicPayment;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayments;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static de.adorsys.aspsp.xs2a.consent.api.pis.PisConsentStatus.*;
import static org.springframework.http.HttpStatus.OK;

@Service
@AllArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    @Qualifier("consentRestTemplate")
    private final RestTemplate consentRestTemplate;
    private final PisConsentRemoteUrls remotePisConsentUrls;
    private final PaymentMapper paymentMapper;
    private final AccountService accountService;

    /**
     * Checks if there is enough funds for payment and if so saves the payment
     *
     * @param payment Single payment
     * @return Optional of saved single payment
     */
    public Optional<SpiSinglePayments> addPayment(@NotNull SpiSinglePayments payment) {
        return areFundsSufficient(payment.getDebtorAccount(), payment.getInstructedAmount().getContent())
                   ? Optional.ofNullable(paymentRepository.save(payment))
                   : Optional.empty();
    }

    /**
     * Saves a periodic payment
     *
     * @param payment Periodic payment
     * @return Optional of saved periodic payment
     */
    public Optional<SpiPeriodicPayment> addPeriodicPayment(@NotNull SpiPeriodicPayment payment) {
        return Optional.ofNullable(paymentRepository.save(payment));
    }

    /**
     * Checks if payment is registered at ASPSP
     *
     * @param paymentId Payments primary ASPSP identifier
     * @return boolean representation of payments presence
     */
    public boolean isPaymentExist(String paymentId) {
        return paymentRepository.exists(paymentId);
    }

    /**
     * Saves a bulk payment
     *
     * @param payments Bulk payment
     * @return list of single payments forming bulk payment
     */
    public List<SpiSinglePayments> addBulkPayments(List<SpiSinglePayments> payments) {
        List<SpiSinglePayments> conductedPayments = new ArrayList<>();
        for (SpiSinglePayments payment : payments) {
            if (areFundsSufficient(payment.getDebtorAccount(), payment.getInstructedAmount().getContent())) {
                payment = paymentRepository.save(payment);
            }
            conductedPayments.add(payment);
        }
        return conductedPayments;
    }

    BigDecimal calculateAmountToBeCharged(String accountId) {
        return paymentRepository.findAll().stream()
                   .filter(paym -> getDebtorAccountIdFromPayment(paym).equals(accountId))
                   .map(this::getAmountFromPayment)
                   .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Optional<SpiSinglePayments> addSinglePaymentWithRedirectApproach(@NotNull String consentId) {
        return Optional.ofNullable(getFirstSpiSinglePayment(getPaymentsFromPisConsent(consentId)))
                   .flatMap(paym -> proceedPayment(paym, consentId));
    }

    //TODO Create GlobalExceptionHandler for error 400 from consentManagement https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/158
    public void revokeOrRejectPaymentConsent(@NotNull String consentId, SpiConsentStatus consentStatus) {
        consentRestTemplate.put(remotePisConsentUrls.updatePisConsentStatus(), null, consentId, consentStatus.name());
    }

    private List<SpiSinglePayments> getPaymentsFromPisConsent(String consentId) {
        ResponseEntity<PisConsentResponse> responseEntity = consentRestTemplate.getForEntity(remotePisConsentUrls.getPisConsentById(), PisConsentResponse.class, consentId);

        if (isPisConsentValid(responseEntity)) {
            return responseEntity.getBody().getPayments().stream()
                       .map(paymentMapper::mapToSpiSinglePayments)
                       .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private boolean isPisConsentValid(ResponseEntity<PisConsentResponse> responseEntity) {
        return responseEntity.getStatusCode() == OK && responseEntity.getBody().getPisConsentStatus() == RECEIVED;
    }

    private SpiSinglePayments getFirstSpiSinglePayment(List<SpiSinglePayments> payments) {
        return CollectionUtils.isNotEmpty(payments)
                   ? payments.get(0)
                   : null;
    }

    //TODO Create GlobalExceptionHandler for error 400 from consentManagement https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/158
    private Optional<SpiSinglePayments> proceedPayment(SpiSinglePayments spiSinglePayments, String consentId) {
        Optional<SpiSinglePayments> saved = Optional.ofNullable(paymentRepository.save(spiSinglePayments));
        consentRestTemplate.put(remotePisConsentUrls.updatePisConsentStatus(), null, consentId, saved.map(s -> VALID).orElse(REJECTED));
        return saved;
    }

    private boolean areFundsSufficient(SpiAccountReference reference, BigDecimal amount) {
        Optional<SpiAccountBalance> balance = Optional.ofNullable(reference)
                                                  .flatMap(this::getInterimAvailableBalanceByReference);
        return balance
                   .map(b -> b.getSpiAmount().getContent().compareTo(amount) > 0)
                   .orElse(false);
    }

    private Optional<SpiAccountBalance> getInterimAvailableBalanceByReference(SpiAccountReference reference) {
        List<SpiAccountDetails> accountsByIban = accountService.getAccountsByIban(reference.getIban());
        return filterDetailsByCurrency(accountsByIban, reference.getCurrency())
                   .flatMap(SpiAccountDetails::getFirstBalance)
                   .map(SpiBalances::getInterimAvailable);
    }

    private Optional<SpiAccountDetails> filterDetailsByCurrency(List<SpiAccountDetails> accounts, Currency currency) {
        return Optional.ofNullable(accounts)
                   .flatMap(accs -> accs.stream()
                                        .filter(ac -> ac.getCurrency() == currency)
                                        .findFirst());
    }

    private String getDebtorAccountIdFromPayment(SpiSinglePayments payment) {
        return Optional.ofNullable(payment.getDebtorAccount())
                   .map(SpiAccountReference::getIban)
                   .orElse("");
    }

    private BigDecimal getAmountFromPayment(SpiSinglePayments payment) {
        return Optional.ofNullable(payment)
                   .map(paym -> getContentFromAmount(payment.getInstructedAmount()))
                   .orElse(BigDecimal.ZERO);
    }

    private BigDecimal getContentFromAmount(SpiAmount amount) {
        return Optional.ofNullable(amount)
                   .map(SpiAmount::getContent)
                   .orElse(BigDecimal.ZERO);
    }
}
