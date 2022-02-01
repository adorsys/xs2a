/*
 * Copyright 2018-2022 adorsys GmbH & Co KG
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version. This program is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 *
 * This project is also available under a separate commercial license. You can
 * contact us at psd2@adorsys.com.
 */

package de.adorsys.psd2.xs2a.domain.pis;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import de.adorsys.psd2.xs2a.core.authorisation.AuthenticationObject;
import de.adorsys.psd2.xs2a.core.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.pis.Xs2aAmount;
import de.adorsys.psd2.xs2a.core.profile.NotificationSupportedMode;
import de.adorsys.psd2.xs2a.core.sca.ChallengeData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.Links;
import de.adorsys.psd2.xs2a.service.spi.InitialSpiAspspConsentDataProvider;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
public abstract class PaymentInitiationResponse {
    private ScaStatus scaStatus;
    @JsonUnwrapped
    private TransactionStatus transactionStatus;
    private Xs2aAmount transactionFees;
    private Boolean transactionFeeIndicator;
    private boolean multilevelScaRequired;
    private String paymentId;
    private List<AuthenticationObject> scaMethods;
    private ChallengeData challengeData;
    private String psuMessage;
    @JsonProperty("_links")
    private Links links = new Links();
    private String authorizationId;
    private InitialSpiAspspConsentDataProvider aspspConsentDataProvider;
    private String aspspAccountId;
    private ErrorHolder errorHolder;
    private String internalRequestId;
    private List<NotificationSupportedMode> tppNotificationContentPreferred;
    private Xs2aAmount currencyConversionFee;
    private Xs2aAmount estimatedTotalAmount;
    private Xs2aAmount estimatedInterbankSettlementAmount;
    private final Set<TppMessageInformation> tppMessageInformation = new HashSet<>();

    PaymentInitiationResponse(ErrorHolder errorHolder) {
        this.errorHolder = errorHolder;
    }

    public boolean hasError() {
        return errorHolder != null;
    }
}
