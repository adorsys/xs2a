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

package de.adorsys.psd2.xs2a.spi.domain.payment.response;

import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.sca.ChallengeData;
import de.adorsys.psd2.xs2a.spi.domain.common.SpiAmount;
import de.adorsys.psd2.xs2a.spi.domain.common.SpiAuthenticationObject;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public abstract class SpiPaymentInitiationResponse {
    private TransactionStatus transactionStatus;
    private String paymentId;
    private SpiAmount spiTransactionFees;
    private Boolean spiTransactionFeeIndicator;
    private boolean multilevelScaRequired;
    private List<SpiAuthenticationObject> scaMethods;
    private String chosenScaMethod;
    private ChallengeData challengeData;
    private String psuMessage;
    private Set<TppMessageInformation> tppMessages;
    private String aspspAccountId;
    private SpiAmount currencyConversionFee;
    private SpiAmount estimatedTotalAmount;
    private SpiAmount estimatedInterbankSettlementAmount;
}
