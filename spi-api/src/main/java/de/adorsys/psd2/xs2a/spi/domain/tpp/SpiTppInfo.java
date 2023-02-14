/*
 * Copyright 2018-2023 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.spi.domain.tpp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.adorsys.psd2.xs2a.core.tpp.TppRedirectUri;
import de.adorsys.psd2.xs2a.core.tpp.TppRole;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class SpiTppInfo {
    @EqualsAndHashCode.Include
    private String authorisationNumber;

    private String tppName;
    @Deprecated // TODO: change with SpiTppRole in 14.8
    private List<TppRole> tppRoles;

    private String authorityId;

    private String authorityName;

    private String country;

    private String organisation;

    private String organisationUnit;

    private String city;

    private String state;

    @Nullable
    @Deprecated // TODO: change with SpiTppRedirectUri in 14.8
    private TppRedirectUri cancelTppRedirectUri;

    private String issuerCN;

    @JsonIgnore
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
