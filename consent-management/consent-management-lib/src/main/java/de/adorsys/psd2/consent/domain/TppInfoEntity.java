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

package de.adorsys.psd2.consent.domain;

import de.adorsys.psd2.xs2a.core.tpp.TppRole;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.NaturalId;

import javax.persistence.*;
import java.util.List;

@Data
@Entity(name = "tpp_info")
@NoArgsConstructor
public class TppInfoEntity extends InstanceDependableEntity {
    @Id
    @Column(name = "tpp_info_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tpp_info_generator")
    @SequenceGenerator(name = "tpp_info_generator", sequenceName = "tpp_info_id_seq", allocationSize = 1)
    private Long id;

    @NaturalId
    @Column(name = "authorisation_number", nullable = false)
    private String authorisationNumber;

    @Column(name = "tpp_name")
    private String tppName;

    @ElementCollection
    @CollectionTable(name = "tpp_info_role", joinColumns = @JoinColumn(name = "tpp_info_id"))
    @Column(name = "tpp_role", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private List<TppRole> tppRoles;

    @Column(name = "authority_id")
    private String authorityId;

    @Column(name = "authority_name")
    private String authorityName;

    @Column(name = "country")
    private String country;

    @Column(name = "organisation")
    private String organisation;

    @Column(name = "organisation_unit")
    private String organisationUnit;

    @Column(name = "city")
    private String city;

    @Column(name = "state")
    private String state;
}
