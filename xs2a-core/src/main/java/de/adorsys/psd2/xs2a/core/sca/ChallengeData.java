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

package de.adorsys.psd2.xs2a.core.sca;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "Challenge data needed for SCA", value = "ChallengeData")
public class ChallengeData {
    @ApiModelProperty(value = "PNG data (max. 512 kilobyte) to be displayed to the PSU, Base64 encoding, cp. [RFC4648]. This attribute is used only, when PHOTO_OTP or CHIP_OTP is the selected SCA method.")
    private byte[] image;

    @ApiModelProperty(value = "String challenge data", example = "zzz")
    private String data;

    @ApiModelProperty(value = "A link where the ASPSP will provides the challenge image for the TPP", example = "https://www.testbank.com/authentication/image.jpg")
    private String imageLink;

    @ApiModelProperty(value = "The maximal length for the OTP to be typed in by the PSU", example = "6")
    private Integer otpMaxLength;

    @ApiModelProperty(value = "The format type of the OTP to be typed in. The admitted values are 'characters' or 'integer'.", example = "integer")
    private OtpFormat otpFormat;

    @ApiModelProperty(value = "Additional explanation for the PSU to explain e.g. fallback mechanism for the chosen SCA method. The TPP is obliged to show this to the PSU.", example = "Additional information")
    private String additionalInformation;

    @JsonIgnore
    public boolean isEmpty() {
        return ArrayUtils.isEmpty(image)
                   && StringUtils.isBlank(data)
                   && StringUtils.isBlank(imageLink)
                   && otpMaxLength == null
                   && otpFormat == null
                   && StringUtils.isBlank(additionalInformation);

    }

}
