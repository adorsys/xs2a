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

package de.adorsys.aspsp.xs2a.domain.pis;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Entity(name = "pis_remittance")
@NoArgsConstructor
@ApiModel(description = "Remittance in pis", value = "PisRemittance")
public class PisRemittance {
    @Id
    @Column(name = "remittance_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pis_remittance_generator")
    @SequenceGenerator(name = "pis_remittance_generator", sequenceName = "pis_remittance_id_seq")
    private Long id;

    @ApiModelProperty(value = "the actual reference", required = true, example = "Ref Number Merchant")
    private String reference;

    @Column(name = "reference_type")
    @ApiModelProperty(value = "reference type", example = "reference type")
    private String referenceType;

    @Column(name = "reference_issuer")
    @ApiModelProperty(value = "reference issuer", example = "reference issuer")
    private String referenceIssuer;
}
