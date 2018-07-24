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

package de.adorsys.aspsp.xs2a.domain;

import de.adorsys.aspsp.xs2a.consent.api.ActionStatus;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.*;
import java.time.LocalDate;

@Data
@Entity(name = "ais_consent_action")
@ApiModel(description = "Ais consent action entity", value = "AisConsentAction")
public class AisConsentAction {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ais_consent_action_generator")
    @SequenceGenerator(name = "ais_consent_action_generator", sequenceName = "ais_consent_action_id_seq")
    private Long id;

    @Column(name = "request_date", nullable = false)
    @ApiModelProperty(value = "Date of the last request for this consent. The content is the local ASPSP date in ISODate Format", required = true, example = "2018-05-04T15:30:35.035Z")
    private LocalDate requestDate;

    @Column(name = "tpp_id", nullable = false)
    @ApiModelProperty(value = "TPP id", required = true, example = "af006545-d713-46d7-b6cf-09c9628f9a5d")
    private String tppId;

    @Column(name = "action_status", nullable = false)
    @Enumerated(value = EnumType.STRING)
    @ApiModelProperty(value = "The following code values are permitted 'SUCCESS', 'BAD_PAYLOAD', 'FAILURE_ACCOUNT', 'FAILURE_BALANCE', 'FAILURE_TRANSACTION', 'FAILURE_PAYMENT'.", required = true, example = "SUCCESS")
    private ActionStatus actionStatus;

    @Column(name = "requested_consent_id", nullable = false)
    @ApiModelProperty(value = "Requested consent ID", required = true, example = "af006545-d713-46d7-b6cf-09c9628f9a5d")
    private String requestedConsentId;
}
