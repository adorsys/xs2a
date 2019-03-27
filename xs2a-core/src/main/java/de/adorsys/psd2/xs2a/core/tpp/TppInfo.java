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

package de.adorsys.psd2.xs2a.core.tpp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Data
@EqualsAndHashCode(of = {"authorisationNumber", "authorityId"})
public class TppInfo {
    @ApiModelProperty(value = "Authorization number", required = true, example = "12345987")
    private String authorisationNumber;

    @ApiModelProperty(value = "Tpp name", required = true, example = "Tpp company")
    private String tppName;

    @ApiModelProperty(value = "Tpp role", required = true)
    private List<TppRole> tppRoles;

    @ApiModelProperty(value = "National competent authority id", required = true, example = "authority id")
    private String authorityId;

    @ApiModelProperty(value = "National competent authority name", required = true, example = "authority name")
    private String authorityName;

    @ApiModelProperty(value = "Country", required = true, example = "Germany")
    private String country;

    @ApiModelProperty(value = "Organisation", required = true, example = "Organisation")
    private String organisation;

    @ApiModelProperty(value = "Organisation unit", required = true, example = "Organisation unit")
    private String organisationUnit;

    @ApiModelProperty(value = "City", required = true, example = "Nuremberg")
    private String city;

    @ApiModelProperty(value = "State", required = true, example = "Bayern")
    private String state;

    @Nullable
    @ApiModelProperty(value = "TPP redirect URIs")
    private TppRedirectUri tppRedirectUri;

    @ApiModelProperty(value = "Issuer CN", required = true, example = "Authority CA Domain Name")
    private String issuerCN;

    @JsonIgnore
    public boolean isNotValid() {
        return !isValid();
    }

    @JsonIgnore
    public boolean isValid() {
        return StringUtils.isNotBlank(authorisationNumber)
                   && StringUtils.isNotBlank(authorityId);
    }
}
