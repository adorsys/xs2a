/*
 * Copyright 2018-2021 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.spi.domain.authorisation;

import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * This class is used as a response to a start SCA flow.
 */
@Value
@EqualsAndHashCode
public class SpiStartAuthorisationResponse {
    /**
     * SCA approach regarding SCA starting.
     */
    @NotNull
    ScaApproach scaApproach;
    /**
     * SCA status regarding SCA starting.
     */
    @NotNull
    ScaStatus scaStatus;
    /**
     * Message to PSU regarding SCA starting.
     */
    @Nullable
    String psuMessage;
    /**
     * Warnings for TPP regarding SCA starting.
     */
    @Nullable
    Set<TppMessageInformation> tppMessages;
}
