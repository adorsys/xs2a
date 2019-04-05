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

package de.adorsys.psd2.xs2a.service.validator.header.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.adorsys.psd2.xs2a.component.AcceptContentTypeDeserializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.http.MediaType;

import javax.validation.constraints.NotNull;

// TODO: should be removed in 2.6 https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/782
@Deprecated
@Data
@ApiModel(description = "Account request header", value = "AccountRequestHeader")
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountRequestHeader extends CommonRequestHeader {

    @ApiModelProperty(value = "ID of the account consent", required = true, example = "5845f9b0-cef6-4f2d-97e0-ed1e0469a907")
    @JsonProperty(value = "consent-id")
    @NotNull
    private String consentId;

    @ApiModelProperty(value = "Is contained only, if an OAuth2 based authentication was performed in a pre-step or an OAuth2 based SCA was performed in the related consent authorisation", required = false, example = "5845f9b0-cef6-4f2d-97e0-ed1e0469a907")
    @JsonProperty(value = "authorization bearer")
    private String authorizationBearer;

    @ApiModelProperty(value = "Indicates the formats of account reports supported together with a prioritisation following the http header definition", required = false, example = "application/json")
    @JsonProperty(value = "accept")
    @JsonDeserialize(using = AcceptContentTypeDeserializer.class)
    private MediaType[] accept;
}
