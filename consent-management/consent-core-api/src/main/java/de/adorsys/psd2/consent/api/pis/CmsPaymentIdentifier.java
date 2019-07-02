/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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

package de.adorsys.psd2.consent.api.pis;

import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.util.Optional;

@Value
@RequiredArgsConstructor
public class CmsPaymentIdentifier {
    private String paymentId;
    private String authorisationId;
    private String tppOkRedirectUri;
    private String tppNokRedirectUri;

    public CmsPaymentIdentifier(CmsPaymentResponse cmsPaymentResponse) {
        this.paymentId = cmsPaymentResponse.getPayment().getPaymentId();
        this.authorisationId = cmsPaymentResponse.getAuthorisationId();
        this.tppOkRedirectUri = Optional.ofNullable(cmsPaymentResponse.getTppOkRedirectUri()).orElse(null);
        this.tppNokRedirectUri = Optional.ofNullable(cmsPaymentResponse.getTppNokRedirectUri()).orElse(null);
    }
}
