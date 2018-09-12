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

package de.adorsys.aspsp.xs2a.service.authorization.pis;

import de.adorsys.aspsp.xs2a.consent.api.pis.authorisation.UpdatePisConsentPsuDataRequest;
import de.adorsys.aspsp.xs2a.domain.consent.Xs2aUpdatePisConsentPsuDataResponse;
import de.adorsys.aspsp.xs2a.domain.consent.Xsa2CreatePisConsentAuthorizationResponse;
import de.adorsys.aspsp.xs2a.domain.pis.PaymentType;

import java.util.Optional;

public interface PisAuthorizationService {
    Optional<Xsa2CreatePisConsentAuthorizationResponse> createConsentAuthorization(String paymentId, PaymentType paymentType);

    Optional<Xs2aUpdatePisConsentPsuDataResponse> updateConsentPsuData(UpdatePisConsentPsuDataRequest request);
}
