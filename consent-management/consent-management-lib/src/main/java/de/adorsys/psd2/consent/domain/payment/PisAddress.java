/*
 * Copyright 2018-2024 adorsys GmbH & Co KG
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
 * contact us at sales@adorsys.com.
 */

package de.adorsys.psd2.consent.domain.payment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Entity(name = "pis_address")
@Schema(description = "Pis address", name = "PIS address")
@NoArgsConstructor
public class PisAddress {
    @Id
    @Column(name = "address_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pis_address_generator")
    @SequenceGenerator(name = "pis_address_generator", sequenceName = "pis_address_id_seq", allocationSize = 1)
    private Long id;

    @Schema(description = "Street", example = "Herrnstraße")
    private String street;

    @Column(name = "building_number")
    @Schema(description = "Building number", example = "123-34")
    private String buildingNumber;

    @Schema(description = "City", example = "Nürnberg")
    private String city;

    @Column(name = "postal_code")
    @Schema(description = "Postal code", example = "90431")
    private String postalCode;

    @Schema(description = "Country")
    private String country;
}
