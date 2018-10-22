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
import de.adorsys.aspsp.aspspmockserver.domain.spi.account.SpiAccountBalance;
import de.adorsys.aspsp.aspspmockserver.domain.spi.account.SpiAccountDetails;
import de.adorsys.aspsp.aspspmockserver.domain.spi.account.SpiAccountReference;
import de.adorsys.aspsp.aspspmockserver.domain.spi.common.SpiAmount;
import de.adorsys.aspsp.aspspmockserver.domain.spi.common.SpiTransactionStatus;
import de.adorsys.aspsp.aspspmockserver.domain.spi.consent.SpiConsentStatus;
import de.adorsys.aspsp.aspspmockserver.domain.spi.payment.*;
import de.adorsys.aspsp.aspspmockserver.repository.PaymentRepository;
import de.adorsys.aspsp.aspspmockserver.service.mapper.PaymentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

import static de.adorsys.aspsp.aspspmockserver.domain.pis.PisPaymentType.PERIODIC;
import static de.adorsys.aspsp.aspspmockserver.domain.pis.PisPaymentType.SINGLE;

@Slf4j
@Service
@RequiredArgsConstructor
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
    public Optional<SpiSinglePayment> addPayment(@NotNull SpiSinglePayment payment) {
        if (areFundsSufficient(payment.getDebtorAccount(), payment.getInstructedAmount().getAmount())) {
            AspspPayment saved = paymentRepository.save(paymentMapper.mapToAspspPayment(payment, SINGLE));
            return Optional.ofNullable(paymentMapper.mapToSpiSinglePayment(saved));
        }

        log.warn("Insufficient funds for paying {} on account {}", payment.getInstructedAmount(), payment.getDebtorAccount());
        return Optional.empty();
    }

    /**
     * Saves a periodic payment
     *
     * @param payment Periodic payment
     * @return Optional of saved periodic payment
     */
    public Optional<SpiPeriodicPayment> addPeriodicPayment(@NotNull SpiPeriodicPayment payment) {
        AspspPayment saved = paymentRepository.save(paymentMapper.mapToAspspPayment(payment, PERIODIC));
        return Optional.ofNullable(paymentMapper.mapToSpiPeriodicPayment(saved));
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
     * Checks payment status
     *
     * @param paymentId Payments primary ASPSP identifier
     * @return SpiPaymentStatus status of payment
     */
    public Optional<SpiTransactionStatus> getPaymentStatusById(String paymentId) {
        List<AspspPayment> payments = paymentRepository.findByPaymentIdOrBulkId(paymentId, paymentId);
        return payments.isEmpty()
                   ? Optional.empty()
                   : Optional.of(payments.get(0).getPaymentStatus());
    }

    /**
     * Saves a bulk payment
     *
     * @param payments Bulk payment
     * @return list of single payments forming bulk payment
     */
    public Optional<SpiBulkPayment> addBulkPayments(SpiBulkPayment payments) {
        List<AspspPayment> aspspPayments = paymentMapper.mapToAspspPaymentList(payments.getPayments());
        Optional<AspspPayment> firstInvalid = aspspPayments.stream()
                                                  .filter(this::isNonExistingAccount)
                                                  .findFirst();

        if (firstInvalid.isPresent()) {
            return Optional.empty();
        }

        List<AspspPayment> savedPayments = paymentRepository.save(aspspPayments);
        SpiBulkPayment result = new SpiBulkPayment();
        result.setPayments(paymentMapper.mapToSpiSinglePaymentList(savedPayments));
        result.setPaymentId(savedPayments.get(0).getBulkId());

        return Optional.of(result);
    }

    private boolean isNonExistingAccount(AspspPayment p) {
        String debtorAccountIdFromPayment = getDebtorAccountIdFromPayment(p);
        return !accountService.getPsuIdByIban(debtorAccountIdFromPayment).isPresent();
    }

    BigDecimal calculateAmountToBeCharged(String accountId) {
        return paymentRepository.findAll().stream()
                   .filter(paym -> getDebtorAccountIdFromPayment(paym).equals(accountId))
                   .map(this::getAmountFromPayment)
                   .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    //TODO Create GlobalExceptionHandler for error 400 from consentManagement https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/158

    /**
     * Updates status of PIS consent
     *
     * @param consentId     Consent primary identifier
     * @param consentStatus New status of the PIS consent
     */
    public void updatePaymentConsentStatus(@NotNull String consentId, SpiConsentStatus consentStatus) {
        consentRestTemplate.put(remotePisConsentUrls.updatePisConsentStatus(), null, consentId, consentStatus.name());
    }

    public List<AspspPayment> getPaymentById(String paymentId) {
        return paymentRepository.findByPaymentIdOrBulkId(paymentId, paymentId);
    }

    /**
     * Cancel payment
     *
     * @param paymentId Payment identifier
     * @return SpiCancelPayment containing information about the requirement of aspsp for start authorisation
     */
    public Optional<SpiCancelPayment> cancelPayment(String paymentId) {
        return Optional.ofNullable(paymentRepository.findOne(paymentId))
                   .map(p -> new SpiCancelPayment());
    }

    public List<AspspPayment> getAllPayments() {
        return paymentRepository.findAll();
    }

    private boolean areFundsSufficient(SpiAccountReference reference, BigDecimal amount) {
        Optional<SpiAccountBalance> balance = Optional.ofNullable(reference)
                                                  .flatMap(this::getInterimAvailableBalanceByReference);
        return balance
                   .map(b -> b.getSpiBalanceAmount().getAmount().compareTo(amount) >= 0)
                   .orElse(false);
    }

    private Optional<SpiAccountBalance> getInterimAvailableBalanceByReference(SpiAccountReference reference) {
        List<SpiAccountDetails> accountsByIban = accountService.getAccountsByIban(reference.getIban());
        return filterDetailsByCurrency(accountsByIban, reference.getCurrency())
                   .flatMap(SpiAccountDetails::getFirstBalance);
    }

    private Optional<SpiAccountDetails> filterDetailsByCurrency(List<SpiAccountDetails> accounts, Currency currency) {
        return Optional.ofNullable(accounts)
                   .flatMap(accs -> accs.stream()
                                        .filter(ac -> ac.getCurrency() == currency)
                                        .findFirst());
    }

    private String getDebtorAccountIdFromPayment(AspspPayment aspspPayment) {
        return Optional.ofNullable(aspspPayment.getDebtorAccount())
                   .map(SpiAccountReference::getIban)
                   .orElse("");
    }

    private BigDecimal getAmountFromPayment(AspspPayment aspspPayment) {
        return Optional.ofNullable(aspspPayment)
                   .map(paym -> getContentFromAmount(aspspPayment.getInstructedAmount()))
                   .orElse(BigDecimal.ZERO);
    }

    private BigDecimal getContentFromAmount(SpiAmount amount) {
        return Optional.ofNullable(amount)
                   .map(SpiAmount::getAmount)
                   .orElse(BigDecimal.ZERO);
    }
}
