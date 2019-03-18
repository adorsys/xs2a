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

package de.adorsys.psd2.consent.domain;

import de.adorsys.psd2.xs2a.core.tpp.TppRole;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Data
@Entity(name = "tpp_info")
@NoArgsConstructor
public class TppInfoEntity extends InstanceDependableEntity {
    @Id
    @Column(name = "tpp_info_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tpp_info_generator")
    @SequenceGenerator(name = "tpp_info_generator", sequenceName = "tpp_info_id_seq")
    private Long id;

    @Column(name = "authorisation_number", nullable = false)
    private String authorisationNumber;

    @Column(name = "tpp_name")
    private String tppName;

    @ElementCollection
    @CollectionTable(name = "tpp_info_role", joinColumns = @JoinColumn(name = "tpp_info_id"))
    @Column(name = "tpp_role", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private List<TppRole> tppRoles;

    @Column(name = "authority_id", nullable = false)
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

    @Column(name = "redirect_uri")
    private String redirectUri;

    @Column(name = "nok_redirect_uri")
    private String nokRedirectUri;
}
