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
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiAmount;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static de.adorsys.aspsp.xs2a.consent.api.pis.PisConsentStatus.RECEIVED;
import static org.springframework.http.HttpStatus.OK;

@Service
@AllArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    @Qualifier("consentRestTemplate")
    private final RestTemplate consentRestTemplate;
    private final PisConsentRemoteUrls remotePisConsentUrls;
    private final PaymentMapper paymentMapper;

    public Optional<SpiSinglePayments> addPayment(@NotNull SpiSinglePayments payment) {
        return Optional.ofNullable(paymentRepository.save(payment));
    }

    public Optional<SpiPeriodicPayment> addPeriodicPayment(@NotNull SpiPeriodicPayment payment) {
        return Optional.ofNullable(paymentRepository.save(payment));
    }

    public boolean isPaymentExist(String paymentId) {
        return paymentRepository.exists(paymentId);
    }

    public List<SpiSinglePayments> addBulkPayments(List<SpiSinglePayments> payments) {
        return paymentRepository.save(payments);
    }

    public BigDecimal calculateAmountToBeCharged(String accountId) {
        return paymentRepository.findAll().stream()
                   .filter(paym -> getDebtorAccountIdFromPayment(paym).equals(accountId))
                   .map(this::getAmountFromPayment)
                   .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Optional<SpiSinglePayments> addPaymentWithRedirectApproach(@NotNull String consentId) {
        return Optional.ofNullable(getFirstSpiSinglePayment(getPaymentsFromPisConsent(consentId)))
                   .map(paym -> proceedPayment(paym, consentId))
                   .orElse(Optional.empty());
    }

    public void revokePaymentConsent(@NotNull String consentId) {
        consentRestTemplate.put(remotePisConsentUrls.updatePisConsentStatus(), null, consentId, "REVOKED_BY_PSU");
    }

    private List<SpiSinglePayments> getPaymentsFromPisConsent(String consentId) {
        ResponseEntity<PisConsentResponse> responseEntity = consentRestTemplate.getForEntity(remotePisConsentUrls.getPisConsentById(), PisConsentResponse.class, consentId);

        if (responseEntity.getStatusCode() == OK && responseEntity.getBody().getPisConsentStatus() == RECEIVED) {
            return responseEntity.getBody().getPayments().stream()
                       .map(pis -> paymentMapper.mapToSpiSinglePayments(pis))
                       .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    private SpiSinglePayments getFirstSpiSinglePayment(List<SpiSinglePayments> payments) {
        return CollectionUtils.isNotEmpty(payments)
                   ? payments.get(0)
                   : null;
    }

    private Optional<SpiSinglePayments> proceedPayment(SpiSinglePayments spiSinglePayments, String consentId) {
        SpiSinglePayments savedPayment = paymentRepository.save(spiSinglePayments);
        if (savedPayment != null) {
            consentRestTemplate.put(remotePisConsentUrls.updatePisConsentStatus(), null, consentId, "VALID");
        } else {
            consentRestTemplate.put(remotePisConsentUrls.updatePisConsentStatus(), null, consentId, "REJECTED");
        }

        return Optional.ofNullable(savedPayment);
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
