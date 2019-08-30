/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Entity(name = "authorisation_template")
@NoArgsConstructor
public class AuthorisationTemplateEntity {

    @Id
    @Column(name = "authorisation_template_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "authorisation_template_generator")
    @SequenceGenerator(name = "authorisation_template_generator", sequenceName = "authorisation_template_id_seq",
        allocationSize = 1)
    private Long id;

    @Column(name = "redirect_uri")
    private String redirectUri;

    @Column(name = "nok_redirect_uri")
    private String nokRedirectUri;

    @Column(name = "cancel_redirect_uri")
    private String cancelRedirectUri;

    @Column(name = "cancel_nok_redirect_uri")
    private String cancelNokRedirectUri;
}
