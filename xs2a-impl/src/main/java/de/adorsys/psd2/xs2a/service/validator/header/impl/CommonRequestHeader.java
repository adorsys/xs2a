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
import de.adorsys.psd2.xs2a.service.validator.header.RequestHeader;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.UUID;

// TODO: should be removed in 2.6 https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/782
@Deprecated
@Data
@ApiModel(description = "Common request header", value = "CommonRequestHeader")
@JsonIgnoreProperties(ignoreUnknown = true)
abstract class CommonRequestHeader implements RequestHeader {

    @NotNull
    @JsonProperty(value = "x-request-id")
    private UUID xRequestId;

    @NotNull
    private String date;

    @JsonProperty(value = "signature")
    private String signature;

    @JsonProperty(value = "tpp-signature-certificate")
    private String tppSignatureCertificate;

    @JsonProperty(value = "tpp-qwac-certificate")
    private String tppQwacCertificate;

    @JsonProperty(value = "Authorization")
    private String bearerToken;

    @JsonProperty(value = "digest")
    private String digest;
}
