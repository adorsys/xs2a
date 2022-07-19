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

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Certificate Data", name = "CertificateRequest")
public class CertificateRequest {

    @Schema(required = true, example = "87B2AC",
        description = "Available in the Public Register of the appropriate National Competent Authority; ")
    @NotNull
    private String authorizationNumber;

    @Schema(required = true, description = "Roles of the PSP")
    @Size(min = 1, max = 3)
    @NotNull
    @Builder.Default
    private List<PspRole> roles = new ArrayList<>();

    @Schema(required = true, example = "Fictional Corporation AG",
        description = "Registered name of your corporation")
    @NotNull
    private String organizationName;

    @Schema(example = "Information Technology")
    private String organizationUnit;

    @Schema(example = "public.corporation.de",
        description = "Domain of your corporation")
    private String domainComponent;

    @Schema(example = "Nuremberg",
        description = "Name of the city of your corporation headquarter")
    private String localityName;

    @Schema(example = "Bayern",
        description = "Name of the state/province of your corporation headquarter")
    private String stateOrProvinceName;

    @Schema(example = "Germany",
        description = "Name of the country your corporation is registered")
    private String countryName;

    @Schema(example = "365",
        description = "Number of days the certificate is valid")
    @Min(1)
    @Max(365)
    @NotNull
    @Builder.Default
    private int validity = 365;

    @NotNull
    @Schema(description = "XS2A Sandbox")
    private String commonName;

}
