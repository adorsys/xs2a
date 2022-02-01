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

package de.adorsys.psd2.certificate.generator.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "Certificate Data", value = "CertificateRequest")
public class CertificateRequest {

    @ApiModelProperty(required = true, example = "87B2AC",
        notes = "Available in the Public Register of the appropriate National Competent Authority; ")
    @NotNull
    private String authorizationNumber;

    @ApiModelProperty(required = true, notes = "Roles of the PSP", position = 1)
    @Size(min = 1, max = 3)
    @NotNull
    @Builder.Default
    private List<PspRole> roles = new ArrayList<>();

    @ApiModelProperty(required = true, example = "Fictional Corporation AG",
        notes = "Registered name of your corporation", position = 1)
    @NotNull
    private String organizationName;

    @ApiModelProperty(example = "Information Technology", notes = "", position = 2)
    private String organizationUnit;

    @ApiModelProperty(example = "public.corporation.de",
        notes = "Domain of your corporation", position = 2)
    private String domainComponent;

    @ApiModelProperty(example = "Nuremberg",
        notes = "Name of the city of your corporation headquarter", position = 2)
    private String localityName;

    @ApiModelProperty(example = "Bayern",
        notes = "Name of the state/province of your corporation headquarter", position = 2)
    private String stateOrProvinceName;

    @ApiModelProperty(example = "Germany",
        notes = "Name of the country your corporation is registered", position = 2)
    private String countryName;

    @ApiModelProperty(example = "365",
        notes = "Number of days the certificate is valid", position = 2)
    @Min(1)
    @Max(365)
    @NotNull
    @Builder.Default
    private int validity = 365;

    @NotNull
    @ApiModelProperty(example = "XS2A Sandbox")
    private String commonName;

}
