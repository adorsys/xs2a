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

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class TppStopListRecord {

    @ApiModelProperty(value = "Authorisation number", required = true, example = "12345987")
    private String tppAuthorisationNumber;

    @ApiModelProperty(value = "National competent authority id", required = true, example = "authority id")
    private String nationalAuthorityId;

    @ApiModelProperty(value = "Status of the TPP in stop list", example = "ENABLED", allowableValues = "ENABLED,BLOCKED")
    private TppStatus status;

    @ApiModelProperty(value = "Blocking expiration datetime", example = "2020-01-01T15:30:35.035Z")
    private OffsetDateTime blockingExpirationTimestamp;

    @ApiModelProperty(value = "Service instance id", example = "instance id")
    private String instanceId;
}
