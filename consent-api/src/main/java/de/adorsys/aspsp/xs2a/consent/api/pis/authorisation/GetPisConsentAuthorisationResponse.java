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

package de.adorsys.aspsp.xs2a.consent.api.pis.authorisation;

import de.adorsys.aspsp.xs2a.consent.api.CmsScaStatus;
import de.adorsys.aspsp.xs2a.consent.api.pis.PisPayment;
import de.adorsys.aspsp.xs2a.consent.api.pis.PisPaymentType;
import lombok.Data;

import java.util.List;

@Data
public class GetPisConsentAuthorisationResponse {
    private String psuId;
    private CmsScaStatus scaStatus;
    private String consentId;
    private String password;
    private List<PisPayment> payments;
    private PisPaymentType paymentType;
    private byte[] aspspData;
}
