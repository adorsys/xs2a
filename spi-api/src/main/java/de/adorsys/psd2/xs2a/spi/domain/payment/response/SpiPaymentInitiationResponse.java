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

import de.adorsys.psd2.xs2a.core.sca.ChallengeData;
import de.adorsys.psd2.xs2a.spi.domain.common.SpiAmount;
import de.adorsys.psd2.xs2a.spi.domain.common.SpiTransactionStatus;
import lombok.Data;

import java.util.List;

@Data
public abstract class SpiPaymentInitiationResponse {
    private SpiTransactionStatus transactionStatus;
    private String paymentId;
    private SpiAmount spiTransactionFees;
    private boolean spiTransactionFeeIndicator;
    private List<String> scaMethods;
    // TODO Make extendable list of scaMethods https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/411
    private String chosenScaMethod;
    private ChallengeData challengeData;
    private String psuMessage;
    private List<String> tppMessages;
    private String aspspAccountId;
}
