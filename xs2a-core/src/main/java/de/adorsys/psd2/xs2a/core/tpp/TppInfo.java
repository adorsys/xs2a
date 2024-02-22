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
    @Schema(description = "Authorization number", required = true, example = "12345987")
    private String authorisationNumber;

    @Schema(description = "Tpp name", required = true, example = "Tpp company")
    private String tppName;

    @Schema(description = "Tpp role", required = true)
    private List<TppRole> tppRoles;

    @Schema(description = "National competent authority id", required = true, example = "authority id")
    private String authorityId;

    @Schema(description = "National competent authority name", required = true, example = "authority name")
    private String authorityName;

    @Schema(description = "Country", required = true, example = "Germany")
    private String country;

    @Schema(description = "Organisation", required = true, example = "Organisation")
    private String organisation;

    @Schema(description = "Organisation unit", required = true, example = "Organisation unit")
    private String organisationUnit;

    @Schema(description = "City", required = true, example = "Nuremberg")
    private String city;

    @Schema(description = "State", required = true, example = "Bayern")
    private String state;

    @Nullable
    @Schema(description = "Cancel TPP redirect URIs")
    private TppRedirectUri cancelTppRedirectUri;

    @Schema(description = "Issuer CN", required = true, example = "Authority CA Domain Name")
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
