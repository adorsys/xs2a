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

package de.adorsys.aspsp.xs2a.service.validator.header.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.adorsys.aspsp.xs2a.service.validator.header.RequestHeader;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@Data
@ApiModel(description = "Common request header", value = "CommonRequestHeader")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
abstract class CommonRequestHeader implements RequestHeader {

    @ApiModelProperty(value = "ID of the tpp request", required = true, example = "f87cdfbe-a35e-407b-93a4-94fe2edc8d2e")
    @JsonProperty(value = "x-request-id")
    @NotNull
    private UUID xRequestId;

    @ApiModelProperty(value = "The timestamp of the tpp request", required = true, example = "Sun, 11 Aug 2019 15:02:37 GMT")
    @JsonProperty(value = "date")
    @NotNull
    private String date;

    @ApiModelProperty(value = "A signature of the request by the TPP on application level. This might be mandated by ASPSP", example = "keyId='Serial_Number_Of_The_TPP’s_certificate',algorithm='rsa- sha256', headers='Digest TPP-Transaction-ID x-request-id PSU-ID Date', signature='Base64(RSA-SHA256(signing string))")
    @JsonProperty(value = "signature")
    private String signature;

    @ApiModelProperty(value = "The certificate used for signing the request, in base64 encoding. It shall be contained if a signature is used, see above", example = "TPP's_eIDAS_Certificate")
    @JsonProperty(value = "tpp-signature-certificate")
    private String tppSignatureCertificate;

    @ApiModelProperty(value = "If OAuth2 has been chosen as pre-step to authenticate the PSU", example = "FJFJWOIEJFOIWEJOEWJ")
    @JsonProperty(value = "Authorization")
    private String bearerToken;

    @ApiModelProperty(value = "Is contained if and only if the Signature element is contained in the header of the request.", example = "730f75dafd73e047b86acb2dbd74e75dcb93272fa084a9082848f2341aa1abb6")
    @JsonProperty(value = "digest")
    private String digest;
}
