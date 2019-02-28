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

import de.adorsys.aspsp.aspspmockserver.domain.pis.AspspPayment;
import de.adorsys.aspsp.aspspmockserver.repository.PaymentRepository;
import de.adorsys.aspsp.aspspmockserver.service.mapper.PaymentMapper;
import de.adorsys.psd2.aspsp.mock.api.account.AspspAccountBalance;
import de.adorsys.psd2.aspsp.mock.api.account.AspspAccountDetails;
import de.adorsys.psd2.aspsp.mock.api.account.AspspAccountReference;
import de.adorsys.psd2.aspsp.mock.api.common.AspspAmount;
import de.adorsys.psd2.aspsp.mock.api.common.AspspTransactionStatus;
import de.adorsys.psd2.aspsp.mock.api.payment.*;
import de.adorsys.psd2.aspsp.mock.api.psu.AspspPsuData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static de.adorsys.aspsp.aspspmockserver.domain.pis.PisPaymentType.PERIODIC;
import static de.adorsys.aspsp.aspspmockserver.domain.pis.PisPaymentType.SINGLE;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final AccountService accountService;
    private final AccountDetailsService accountDetailsService;

    /**
     * Creates new payment
     *
     * @param aspspPaymentInfo payment
     * @return Optional of saved single payment
     */
    public Optional<AspspPaymentInfo> addPaymentInfo(AspspPaymentInfo aspspPaymentInfo) {
        AspspPayment saved = paymentRepository.save(paymentMapper.mapToAspspPayment(aspspPaymentInfo));
        return Optional.ofNullable(paymentMapper.mapToAspspPaymentInfo(saved));
    }

    /**
     * Checks if there is enough funds for payment and if so saves the payment
     *
     * @param payment Single payment
     * @return Optional of saved single payment
     */
    public Optional<AspspSinglePayment> addPayment(AspspSinglePayment payment) {
        if (payment.getInstructedAmount() != null && areFundsSufficient(payment.getDebtorAccount(), payment.getInstructedAmount().getAmount())) {
            payment.setDebtorAccount(enrichAccountReferenceWithAspspAccountId(payment.getDebtorAccount()));
            payment.setPsuDataList(readAccountPsuData(payment.getDebtorAccount()));

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
    public Optional<AspspPeriodicPayment> addPeriodicPayment(AspspPeriodicPayment payment) {
        payment.setDebtorAccount(enrichAccountReferenceWithAspspAccountId(payment.getDebtorAccount()));
        payment.setPsuDataList(readAccountPsuData(payment.getDebtorAccount()));

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
        String bulkId = StringUtils.isBlank(payments.getPaymentId())
                            ? UUID.randomUUID().toString()
                            : payments.getPaymentId();

        List<AspspPayment> aspspPayments = paymentMapper.mapToAspspPaymentList(payments.getPayments(), bulkId);
        Optional<AspspPayment> firstInvalid = aspspPayments.stream()
                                                  .filter(this::isNonExistingAccount)
                                                  .findFirst();

        if (firstInvalid.isPresent()) {
            return Optional.empty();
        }

        AspspAccountReference debtorAccount = getDebtorAccountFromPayments(aspspPayments);
        BigDecimal totalAmount = calculateTotalAmount(aspspPayments);
        if (!areFundsSufficient(debtorAccount, totalAmount)) {
            log.warn("Insufficient funds for paying {} on account {}", totalAmount, debtorAccount);
            return Optional.empty();
        }

        List<AspspPayment> savedPayments = paymentRepository.save(aspspPayments);
        AspspBulkPayment result = new AspspBulkPayment();
        result.setPayments(paymentMapper.mapToAspspSinglePaymentList(savedPayments));
        result.setPaymentId(savedPayments.get(0).getBulkId());
        result.setDebtorAccount(enrichAccountReferenceWithAspspAccountId(payments.getDebtorAccount()));
        result.setPsuDataList(readAccountPsuData(result.getPayments().get(0).getDebtorAccount()));

        return Optional.of(result);
    }

    private AspspAccountReference getDebtorAccountFromPayments(List<AspspPayment> aspspPayments) {
        return aspspPayments.stream()
                   .findFirst()
                   .map(AspspPayment::getDebtorAccount)
                   .orElse(null);
    }

    private BigDecimal calculateTotalAmount(List<AspspPayment> payments) {
        return payments.stream()
                   .map(this::getAmountFromPayment)
                   .reduce(BigDecimal.ZERO, BigDecimal::add);
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
     * Gets payments by paymentId
     *
     * @param paymentId Payment identifier
     * @return AspspPayment information about payment
     */
    public List<AspspPayment> getPaymentById(String paymentId) {
        return paymentRepository.findByPaymentIdOrBulkId(paymentId, paymentId);
    }

    /**
     * Gets a common payment by paymentId
     *
     * @param paymentId Payment identifier
     * @return AspspPaymentInfo common information about payment
     */
    public Optional<AspspPaymentInfo> getCommonPaymentById(String paymentId) {
        return paymentRepository.findByPaymentId(paymentId)
                   .map(paymentMapper::mapToAspspPaymentInfo);
    }

    /**
     * Cancels payment
     *
     * @param paymentId Payment identifier
     * @return AspspPaymentCancellationResponse containing information about the requirement of aspsp for start authorisation
     */
    public Optional<AspspPaymentCancellationResponse> cancelPayment(String paymentId) {
        List<AspspPayment> payments = paymentRepository.findByPaymentIdOrBulkId(paymentId, paymentId);
        if (CollectionUtils.isEmpty(payments)) {
            return Optional.empty();
        }

        payments.forEach(
            payment -> updateAspsPaymentStatus(payment, AspspTransactionStatus.CANC)
        );

        return Optional.of(getPaymentCancellationResponse(false, AspspTransactionStatus.CANC));
    }

    /**
     * Initiates payment cancellation process
     *
     * @param paymentId Payment identifier
     * @return SpiCancelPayment containing information about the requirement of aspsp for start authorisation
     */
    public Optional<AspspPaymentCancellationResponse> initiatePaymentCancellation(String paymentId) {
        List<AspspPayment> payments = paymentRepository.findByPaymentIdOrBulkId(paymentId, paymentId);
        if (CollectionUtils.isEmpty(payments)) {
            return Optional.empty();
        }

        return Optional.of(getPaymentCancellationResponse(true, payments.get(0).getPaymentStatus()));
    }

    /**
     * Gets all payments
     *
     * @return List of payments
     */
    public List<AspspPayment> getAllPayments() {
        return paymentRepository.findAll();
    }

    private AspspPayment updateAspsPaymentStatus(AspspPayment payment, AspspTransactionStatus transactionStatus) {
        payment.setPaymentStatus(transactionStatus);
        return paymentRepository.save(payment);
    }

    private AspspPaymentCancellationResponse getPaymentCancellationResponse(boolean cancellationAuthorisationMandated,
                                                                            AspspTransactionStatus transactionStatus) {
        AspspPaymentCancellationResponse response = new AspspPaymentCancellationResponse();
        response.setCancellationAuthorisationMandated(cancellationAuthorisationMandated);
        response.setTransactionStatus(transactionStatus);
        return response;
    }

    private boolean areFundsSufficient(AspspAccountReference reference, BigDecimal amount) {
        return Optional.ofNullable(reference)
                   .map(this::getAvailableBalanceByReference)
                   .map(am -> am.compareTo(amount) >= 0)
                   .orElse(false);
    }

    private BigDecimal getAvailableBalanceByReference(AspspAccountReference reference) {
        BigDecimal amountZero = BigDecimal.ZERO;
        List<AspspAccountDetails> accounts = accountService.getAccountsByIban(reference.getIban());

        if (CollectionUtils.isNotEmpty(accounts)) {
            return accounts.stream()
                       .filter(ac -> ac.getCurrency() == reference.getCurrency())
                       .findFirst()
                       .flatMap(AspspAccountDetails::getFirstBalance)
                       .map(AspspAccountBalance::getSpiBalanceAmount)
                       .map(AspspAmount::getAmount)
                       .orElse(amountZero);
        }
        return amountZero;
    }

    private String getDebtorAccountIdFromPayment(AspspPayment aspspPayment) {
        return Optional.ofNullable(aspspPayment.getDebtorAccount())
                   .map(AspspAccountReference::getIban)
                   .orElse("");
    }

    private BigDecimal getAmountFromPayment(AspspPayment aspspPayment) {
        return Optional.ofNullable(aspspPayment)
                   .map(paym -> getContentFromAmount(paym.getInstructedAmount()))
                   .orElse(BigDecimal.ZERO);
    }

    private BigDecimal getContentFromAmount(AspspAmount amount) {
        return Optional.ofNullable(amount)
                   .map(AspspAmount::getAmount)
                   .orElse(BigDecimal.ZERO);
    }

    private AspspAccountReference enrichAccountReferenceWithAspspAccountId(AspspAccountReference reference) {
        if (reference != null) {
            accountService.getAccountIdByIbanAndCurrency(reference.getIban(), reference.getCurrency())
                .ifPresent(reference::setAccountId);
        }

        return reference;
    }

    private List<AspspPsuData> readAccountPsuData(AspspAccountReference accountReference) {
        return accountDetailsService.getPsuDataByAccount(accountReference);
    }
}
