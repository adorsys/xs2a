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

package de.adorsys.aspsp.xs2a.domain.consent.pis;

import de.adorsys.aspsp.xs2a.domain.ErrorHolder;
import de.adorsys.aspsp.xs2a.domain.Links;
import de.adorsys.aspsp.xs2a.domain.consent.Xs2aAuthenticationObject;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import lombok.Data;

import java.util.List;

@Data
public class Xs2aUpdatePisConsentPsuDataResponse {
    private String psuId;
    private ErrorHolder errorHolder;
    private String psuMessage;
    private String paymentId;
    private String authorisationId;

    private ScaStatus scaStatus;
    private List<Xs2aAuthenticationObject> availableScaMethods;
    private Xs2aAuthenticationObject chosenScaMethod;
    private Links links = new Links();

    public Xs2aUpdatePisConsentPsuDataResponse(ScaStatus scaStatus, List<Xs2aAuthenticationObject> availableScaMethods) {
        this.scaStatus = scaStatus;
        this.availableScaMethods = availableScaMethods;
    }

    public Xs2aUpdatePisConsentPsuDataResponse(ScaStatus scaStatus) {
        this(scaStatus, null);
    }

    public Xs2aUpdatePisConsentPsuDataResponse(ErrorHolder errorHolder) {
        this(ScaStatus.FAILED);
        this.errorHolder = errorHolder;
    }

    public boolean hasError() {
        return errorHolder != null;
    }
}

