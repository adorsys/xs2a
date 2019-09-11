/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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

package de.adorsys.psd2.aspsp.profile.domain.pis;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PisRedirectLinkBankSetting {

    /**
     * URL to online-banking to authorise payment with redirect approach
     */
    private String pisRedirectUrlToAspsp;

    /**
     * URL to online-banking to authorise payment cancellation with redirect approach
     */
    private String pisPaymentCancellationRedirectUrlToAspsp;

    /**
     * Contains the limit of an expiration time of redirect url for payment cancellation set in milliseconds
     */
    private long paymentCancellationRedirectUrlExpirationTimeMs;
}
