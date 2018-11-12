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

package de.adorsys.psd2.xs2a.service.payment;

import de.adorsys.psd2.xs2a.core.profile.PaymentProduct;
import de.adorsys.psd2.xs2a.domain.TppInfo;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aPisConsent;
import de.adorsys.psd2.xs2a.domain.pis.*;

public interface ScaPaymentService {
    SinglePaymentInitiationResponse createSinglePayment(SinglePayment singlePayment, TppInfo tppInfo, PaymentProduct paymentProduct, Xs2aPisConsent pisConsent);

    PeriodicPaymentInitiationResponse createPeriodicPayment(PeriodicPayment periodicPayment, TppInfo tppInfo, PaymentProduct paymentProduct, Xs2aPisConsent pisConsent);

    BulkPaymentInitiationResponse createBulkPayment(BulkPayment bulkPayment, TppInfo tppInfo, PaymentProduct paymentProduct, Xs2aPisConsent pisConsent);
}
