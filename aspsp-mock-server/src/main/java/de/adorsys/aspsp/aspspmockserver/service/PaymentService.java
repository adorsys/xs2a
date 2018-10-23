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
import de.adorsys.aspsp.aspspmockserver.domain.pis.AspspPayment;
import de.adorsys.aspsp.aspspmockserver.repository.PaymentRepository;
import de.adorsys.aspsp.aspspmockserver.service.mapper.PaymentMapper;
import de.adorsys.psd2.aspsp.mock.api.account.AspspAccountBalance;
import de.adorsys.psd2.aspsp.mock.api.account.AspspAccountDetails;
import de.adorsys.psd2.aspsp.mock.api.account.AspspAccountReference;
import de.adorsys.psd2.aspsp.mock.api.common.AspspAmount;
import de.adorsys.psd2.aspsp.mock.api.common.AspspTransactionStatus;
import de.adorsys.psd2.aspsp.mock.api.consent.AspspConsentStatus;
import de.adorsys.psd2.aspsp.mock.api.payment.AspspBulkPayment;
import de.adorsys.psd2.aspsp.mock.api.payment.AspspCancelPayment;
import de.adorsys.psd2.aspsp.mock.api.payment.AspspPeriodicPayment;
import de.adorsys.psd2.aspsp.mock.api.payment.AspspSinglePayment;
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
    public Optional<AspspSinglePayment> addPayment(@NotNull AspspSinglePayment payment) {
        if (payment.getInstructedAmount() != null && areFundsSufficient(payment.getDebtorAccount(), payment.getInstructedAmount().getAmount())) {
            AspspPayment saved = paymentRepository.save(paymentMapper.mapToAspspPayment(payment, SINGLE));
            return Optional.ofNullable(paymentMapper.mapToAspspSinglePayment(saved));
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
    public Optional<AspspPeriodicPayment> addPeriodicPayment(@NotNull AspspPeriodicPayment payment) {
        AspspPayment saved = paymentRepository.save(paymentMapper.mapToAspspPayment(payment, PERIODIC));
        return Optional.ofNullable(paymentMapper.mapToAspspPeriodicPayment(saved));
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
     * @return AspspPaymentStatus status of payment
     */
    public Optional<AspspTransactionStatus> getPaymentStatusById(String paymentId) {
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
    public Optional<AspspBulkPayment> addBulkPayments(AspspBulkPayment payments) {
        List<AspspPayment> aspspPayments = paymentMapper.mapToAspspPaymentList(payments.getPayments());
        Optional<AspspPayment> firstInvalid = aspspPayments.stream()
                                                  .filter(this::isNonExistingAccount)
                                                  .findFirst();

        if (firstInvalid.isPresent()) {
            return Optional.empty();
        }

        List<AspspPayment> savedPayments = paymentRepository.save(aspspPayments);
        AspspBulkPayment result = new AspspBulkPayment();
        result.setPayments(paymentMapper.mapToAspspSinglePaymentList(savedPayments));
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
    public void updatePaymentConsentStatus(@NotNull String consentId, AspspConsentStatus consentStatus) {
        consentRestTemplate.put(remotePisConsentUrls.updatePisConsentStatus(), null, consentId, consentStatus.name());
    }

    public List<AspspPayment> getPaymentById(String paymentId) {
        return paymentRepository.findByPaymentIdOrBulkId(paymentId, paymentId);
    }

    /**
     * Cancel payment
     *
     * @param paymentId Payment identifier
     * @return AspspCancelPayment containing information about the requirement of aspsp for start authorisation
     */
    public Optional<AspspCancelPayment> cancelPayment(String paymentId) {
        return Optional.ofNullable(paymentRepository.findOne(paymentId))
                   .map(p -> new AspspCancelPayment());
    }

    public List<AspspPayment> getAllPayments() {
        return paymentRepository.findAll();
    }

    private boolean areFundsSufficient(AspspAccountReference reference, BigDecimal amount) {
        Optional<AspspAccountBalance> balance = Optional.ofNullable(reference)
                                                  .flatMap(this::getInterimAvailableBalanceByReference);
        return balance
                   .map(b -> b.getSpiBalanceAmount().getAmount().compareTo(amount) >= 0)
                   .orElse(false);
    }

    private Optional<AspspAccountBalance> getInterimAvailableBalanceByReference(AspspAccountReference reference) {
        List<AspspAccountDetails> accountsByIban = accountService.getAccountsByIban(reference.getIban());
        return filterDetailsByCurrency(accountsByIban, reference.getCurrency())
                   .flatMap(AspspAccountDetails::getFirstBalance);
    }

    private Optional<AspspAccountDetails> filterDetailsByCurrency(List<AspspAccountDetails> accounts, Currency currency) {
        return Optional.ofNullable(accounts)
                   .flatMap(accs -> accs.stream()
                                        .filter(ac -> ac.getCurrency() == currency)
                                        .findFirst());
    }

    private String getDebtorAccountIdFromPayment(AspspPayment aspspPayment) {
        return Optional.ofNullable(aspspPayment.getDebtorAccount())
                   .map(AspspAccountReference::getIban)
                   .orElse("");
    }

    private BigDecimal getAmountFromPayment(AspspPayment aspspPayment) {
        return Optional.ofNullable(aspspPayment)
                   .map(paym -> getContentFromAmount(aspspPayment.getInstructedAmount()))
                   .orElse(BigDecimal.ZERO);
    }

    private BigDecimal getContentFromAmount(AspspAmount amount) {
        return Optional.ofNullable(amount)
                   .map(AspspAmount::getAmount)
                   .orElse(BigDecimal.ZERO);
    }
}
