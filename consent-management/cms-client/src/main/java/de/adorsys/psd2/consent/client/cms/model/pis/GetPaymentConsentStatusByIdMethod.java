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

package de.adorsys.psd2.consent.client.cms.model.pis;

import de.adorsys.psd2.consent.api.PisConsentStatusResponse;
import de.adorsys.psd2.consent.client.cms.RestCmsRequestMethod;
import de.adorsys.psd2.consent.client.core.HttpMethod;
import de.adorsys.psd2.consent.client.core.util.HttpUriParams;

public class GetPaymentConsentStatusByIdMethod extends RestCmsRequestMethod<Void, PisConsentStatusResponse> {
    private static final String GET_PAYMENT_CONSENT_STATUS_BY_ID_URI = "api/v1/pis/consent/{consent-id}/status";

    public GetPaymentConsentStatusByIdMethod(HttpUriParams uriParams) {
        super(HttpMethod.GET, GET_PAYMENT_CONSENT_STATUS_BY_ID_URI, uriParams);
    }
}
