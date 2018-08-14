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

package de.adorsys.aspsp.cmsclient.cms.model.pis;

import de.adorsys.aspsp.cmsclient.cms.RestCmsRequestMethod;
import de.adorsys.aspsp.cmsclient.core.HttpMethod;
import de.adorsys.aspsp.cmsclient.core.util.HttpUriParams;

public class UpdatePaymentConsentStatusMethod extends RestCmsRequestMethod<Void, Void> {
    private static final String UPDATE_PAYMENT_CONSENT_STATUS_URI = "api/v1/pis/consent/{consent-id}/status/{status}";

    public UpdatePaymentConsentStatusMethod(HttpUriParams uriParams) {
        super(HttpMethod.PUT, UPDATE_PAYMENT_CONSENT_STATUS_URI, uriParams);
    }
}
