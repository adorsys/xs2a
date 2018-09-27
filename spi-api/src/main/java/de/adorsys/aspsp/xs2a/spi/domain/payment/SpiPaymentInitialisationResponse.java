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

package de.adorsys.aspsp.xs2a.spi.domain.payment;

import de.adorsys.aspsp.xs2a.spi.domain.common.SpiAmount;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiTransactionStatus;
import lombok.Data;

@Data
public class SpiPaymentInitialisationResponse {
    private SpiTransactionStatus transactionStatus;
    private String paymentId;
    private SpiAmount spiTransactionFees;
    private boolean spiTransactionFeeIndicator;
    private String[] scaMethods;
    private String chosenScaMethod;
    private SpiChallengeData challengeData;
    private String psuMessage;
    private String[] tppMessages;
    private boolean tppRedirectPreferred;
}
