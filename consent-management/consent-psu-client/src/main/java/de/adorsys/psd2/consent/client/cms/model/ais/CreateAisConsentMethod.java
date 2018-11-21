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

package de.adorsys.psd2.consent.client.cms.model.ais;

import de.adorsys.psd2.consent.api.ais.CreateAisConsentRequest;
import de.adorsys.psd2.consent.api.ais.CreateAisConsentResponse;
import de.adorsys.psd2.consent.client.cms.RestCmsRequestMethod;
import de.adorsys.psd2.consent.client.core.HttpMethod;

public class CreateAisConsentMethod extends RestCmsRequestMethod<CreateAisConsentRequest, CreateAisConsentResponse> {
    private static final String CREATE_AIS_CONSENT_URI = "api/v1/ais/consent/";

    public CreateAisConsentMethod(final CreateAisConsentRequest request) {
        super(request, HttpMethod.POST, CREATE_AIS_CONSENT_URI);
    }
}
