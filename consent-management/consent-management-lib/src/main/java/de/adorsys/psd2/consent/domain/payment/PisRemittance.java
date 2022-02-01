/*
 * Copyright 2018-2022 adorsys GmbH & Co KG
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

package de.adorsys.psd2.consent.domain.payment;

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
    @SequenceGenerator(name = "pis_remittance_generator", sequenceName = "pis_remittance_id_seq", allocationSize = 1)
    private Long id;

    @ApiModelProperty(value = "The actual reference", required = true, example = "Ref Number Merchant")
    private String reference;

    @Column(name = "reference_type")
    @ApiModelProperty(value = "Reference type", example = "reference type")
    private String referenceType;

    @Column(name = "reference_issuer")
    @ApiModelProperty(value = "Reference issuer", example = "reference issuer")
    private String referenceIssuer;
}
