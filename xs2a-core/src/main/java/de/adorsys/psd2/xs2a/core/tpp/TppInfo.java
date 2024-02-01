/*
 * Copyright 2018-2024 adorsys GmbH & Co KG
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
 * contact us at sales@adorsys.com.
 */

package de.adorsys.psd2.xs2a.core.tpp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TppInfo {
    @EqualsAndHashCode.Include
    @Schema(description = "Authorization number", requiredMode = Schema.RequiredMode.REQUIRED, example = "12345987")
    private String authorisationNumber;

    @Schema(description = "TPP name", requiredMode = Schema.RequiredMode.REQUIRED, example = "Tpp company")
    private String tppName;

    @Schema(description = "TPP role", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<TppRole> tppRoles;

    @Schema(description = "National competent authority ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "authority id")
    private String authorityId;

    @Schema(description = "National competent authority name", requiredMode = Schema.RequiredMode.REQUIRED, example = "authority name")
    private String authorityName;

    @Schema(description = "Country", requiredMode = Schema.RequiredMode.REQUIRED, example = "Germany")
    private String country;

    @Schema(description = "Organisation", requiredMode = Schema.RequiredMode.REQUIRED, example = "Organisation")
    private String organisation;

    @Schema(description = "Organisation unit", requiredMode = Schema.RequiredMode.REQUIRED, example = "Organisation unit")
    private String organisationUnit;

    @Schema(description = "City", requiredMode = Schema.RequiredMode.REQUIRED, example = "Nuremberg")
    private String city;

    @Schema(description = "State", requiredMode = Schema.RequiredMode.REQUIRED, example = "Bayern")
    private String state;

    @Nullable
    @Schema(description = "Cancel TPP redirect URIs")
    private TppRedirectUri cancelTppRedirectUri;

    @Schema(description = "Issuer CN", requiredMode = Schema.RequiredMode.REQUIRED, example = "Authority CA Domain Name")
    private String issuerCN;

    @JsonIgnore
    @Schema(description = "List of DNS which are stored in `Subject Alternative Name` field in QWAC")
    private List<String> dnsList = new ArrayList<>();

    @JsonIgnore
    public boolean isNotValid() {
        return !isValid();
    }

    @JsonIgnore
    public boolean isValid() {
        return StringUtils.isNotBlank(authorisationNumber);
    }
}
