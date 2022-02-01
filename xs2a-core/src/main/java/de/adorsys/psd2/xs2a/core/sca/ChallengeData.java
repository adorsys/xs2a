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

package de.adorsys.psd2.xs2a.core.sca;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "Challenge data needed for SCA", value = "ChallengeData")
public class ChallengeData {
    @ApiModelProperty(value = "PNG data (max. 512 kilobyte) to be displayed to the PSU, Base64 encoding, cp. [RFC4648]. This attribute is used only, when PHOTO_OTP or CHIP_OTP is the selected SCA method.")
    private byte[] image;

    @ApiModelProperty(value = "String challenge data", example = "zzz")
    private List<String> data;

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
                   && CollectionUtils.isEmpty(data)
                   && StringUtils.isBlank(imageLink)
                   && otpMaxLength == null
                   && otpFormat == null
                   && StringUtils.isBlank(additionalInformation);

    }

}
