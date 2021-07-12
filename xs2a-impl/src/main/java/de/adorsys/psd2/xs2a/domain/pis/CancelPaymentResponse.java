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

package de.adorsys.psd2.xs2a.domain.pis;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.adorsys.psd2.xs2a.core.authorisation.AuthenticationObject;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ChallengeData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.Links;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aChosenScaMethod;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
public class CancelPaymentResponse {
    private boolean startAuthorisationRequired;
    private TransactionStatus transactionStatus;
    private List<AuthenticationObject> scaMethods;
    private Xs2aChosenScaMethod chosenScaMethod;
    private ChallengeData challengeData;

    private String paymentId;
    private PaymentType paymentType;
    private String paymentProduct;
    private String authorizationId;
    private ScaStatus scaStatus;
    private PsuIdData psuData;

    @NotNull
    @JsonProperty("_links")
    private Links links;
    private String internalRequestId;
    private Set<TppMessageInformation> tppMessageInformation = new HashSet<>();
    private String psuMessage;
}
