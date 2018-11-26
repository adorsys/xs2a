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

package de.adorsys.psd2.xs2a.service.consent;

import de.adorsys.psd2.consent.api.pis.proto.CreatePisConsentResponse;
import de.adorsys.psd2.consent.api.pis.proto.PisConsentRequest;
import de.adorsys.psd2.consent.api.pis.proto.PisConsentResponse;
import de.adorsys.psd2.consent.api.service.PisConsentService;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.pis.BulkPayment;
import de.adorsys.psd2.xs2a.domain.pis.PaymentInitiationParameters;
import de.adorsys.psd2.xs2a.domain.pis.PeriodicPayment;
import de.adorsys.psd2.xs2a.domain.pis.SinglePayment;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aPisConsentMapper;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aToCmsPisConsentRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class Xs2aPisConsentService {
    private final PisConsentService pisConsentService;
    private final Xs2aPisConsentMapper pisConsentMapper;
    private final Xs2aToCmsPisConsentRequest xs2aToCmsPisConsentRequest;

    /**
     * Creates PIS consent
     *
     * @param parameters Payment request parameters to get needed payment info
     * @param tppInfo    information about TPP
     * @return String consentId
     */
    // TODO refactoring for orElse(null)
    public CreatePisConsentResponse createPisConsent(PaymentInitiationParameters parameters, TppInfo tppInfo) {
        PisConsentRequest request = new PisConsentRequest();
        request.setTppInfo(tppInfo);
        request.setPaymentProduct(parameters.getPaymentProduct());
        request.setPaymentType(parameters.getPaymentType());
        request.setPsuData(parameters.getPsuData());
        return pisConsentService.createPaymentConsent(request)
                   .orElse(null);
    }

    public Optional<PisConsentResponse> getPisConsentById(String consentId) {
        return pisConsentService.getConsentById(consentId);
    }

    public void updateSinglePaymentInPisConsent(SinglePayment singlePayment, PaymentInitiationParameters paymentInitiationParameters, String consentId) {
        PisConsentRequest pisConsentRequest = xs2aToCmsPisConsentRequest.mapToCmsSinglePisConsentRequest(singlePayment, paymentInitiationParameters.getPaymentProduct());
        pisConsentService.updatePaymentConsent(pisConsentRequest, consentId);
    }

    public void updatePeriodicPaymentInPisConsent(PeriodicPayment periodicPayment, PaymentInitiationParameters paymentInitiationParameters, String consentId) {
        PisConsentRequest pisConsentRequest = xs2aToCmsPisConsentRequest.mapToCmsPeriodicPisConsentRequest(periodicPayment, paymentInitiationParameters.getPaymentProduct());
        pisConsentService.updatePaymentConsent(pisConsentRequest, consentId);
    }

    public void updateBulkPaymentInPisConsent(BulkPayment bulkPayment, PaymentInitiationParameters paymentInitiationParameters, String consentId) {
        PisConsentRequest pisConsentRequest = xs2aToCmsPisConsentRequest.mapToCmsBulkPisConsentRequest(bulkPayment, paymentInitiationParameters.getPaymentProduct());
        pisConsentService.updatePaymentConsent(pisConsentRequest, consentId);
    }

    public Optional<Boolean> revokeConsentById(String consentId) {
        return pisConsentService.updateConsentStatusById(consentId, ConsentStatus.REVOKED_BY_PSU);
    }
}
