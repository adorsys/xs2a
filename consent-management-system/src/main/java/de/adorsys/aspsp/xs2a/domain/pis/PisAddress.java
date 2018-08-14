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
@Entity(name = "pis_address")
@ApiModel(description = "Pis address", value = "Pis address")
@NoArgsConstructor
public class PisAddress {
    @Id
    @Column(name = "address_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pis_address_generator")
    @SequenceGenerator(name = "pis_address_generator", sequenceName = "pis_address_id_seq")
    private Long id;

    @ApiModelProperty(value = "Street", example = "Herrnstraße")
    private String street;

    @Column(name = "building_number")
    @ApiModelProperty(value = "Building number", example = "123-34")
    private String buildingNumber;

    @ApiModelProperty(value = "City", example = "Nürnberg")
    private String city;

    @Column(name = "postal_code")
    @ApiModelProperty(value = "Postal code", example = "90431")
    private String postalCode;

    @ApiModelProperty(value = "Country")
    private String country;
}
