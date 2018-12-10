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

import de.adorsys.psd2.xs2a.core.tpp.TppStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@Entity(name = "tpp_stop_list")
public class TppStopListEntity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tpp_stop_list_generator")
    @SequenceGenerator(name = "tpp_stop_list_generator", sequenceName = "tpp_stop_list_id_seq")
    private Long id;

    @Column(name = "tpp_authorisation_number", nullable = false)
    private String tppAuthorisationNumber;

    @Column(name = "authority_id", nullable = false)
    private String authorityId;

    @Column(name = "status", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private TppStatus status;

    @Column(name = "expiration_timestamp")
    private OffsetDateTime blockingExpirationTimestamp;
}
