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

import de.adorsys.aspsp.xs2a.consent.api.UpdateConsentAspspDataRequest;
import de.adorsys.aspsp.xs2a.consent.api.pis.proto.CreatePisConsentResponse;
import de.adorsys.psd2.consent.client.cms.RestCmsRequestMethod;
import de.adorsys.psd2.consent.client.core.HttpMethod;
import de.adorsys.psd2.consent.client.core.util.HttpUriParams;

public class UpdatePisConsentAspspDataMethod extends RestCmsRequestMethod<UpdateConsentAspspDataRequest, CreatePisConsentResponse> {
    private static final String UPDATE_PIS_CONSENT_BLOB_URI = "api/v1/pis/consent/{consent-id}/aspspConsentData";

    public UpdatePisConsentAspspDataMethod(final UpdateConsentAspspDataRequest request, HttpUriParams uriParams) {
        super(request, HttpMethod.PUT, UPDATE_PIS_CONSENT_BLOB_URI, uriParams);
    }
}
