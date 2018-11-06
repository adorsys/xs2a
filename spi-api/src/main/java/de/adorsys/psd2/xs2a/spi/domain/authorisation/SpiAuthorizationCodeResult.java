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

package de.adorsys.psd2.xs2a.spi.domain.authorisation;

import de.adorsys.psd2.xs2a.spi.domain.payment.SpiOtpFormat;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class SpiAuthorizationCodeResult {
    private byte [] image;
    private String data;
    private String imageLink;
    private Integer otpMaxLength;
    private SpiOtpFormat otpFormat;
    private String additionalInformation;

    public boolean isEmpty() {
        return image == null
            && StringUtils.isEmpty(data)
            && StringUtils.isEmpty(imageLink)
            && otpMaxLength == null
            && otpFormat == null
            && StringUtils.isEmpty(additionalInformation);
    }
}
