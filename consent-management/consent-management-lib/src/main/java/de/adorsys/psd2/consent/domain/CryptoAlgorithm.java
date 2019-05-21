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

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Entity(name = "crypto_algorithm")
@NoArgsConstructor
public class CryptoAlgorithm {
    @Id
    @Column(name = "algorithm_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "crypto_algorithm_generator")
    @SequenceGenerator(name = "crypto_algorithm_generator", sequenceName = "crypto_algorithm_id_seq", allocationSize = 1)
    private Long id;

    @Column(name = "external_id", nullable = false, unique = true)
    private String externalId;

    @Column(name = "algorithm", nullable = false)
    private String algorithm;

    @Column(name = "version", nullable = false)
    private String version;

    @Column(name = "key_length_in_bytes", nullable = false)
    private int keyLength;

    @Column(name = "hash_iterations", nullable = false)
    private int hashIterations;

    @Column(name = "skf_algorithm", nullable = false)
    private String skfAlgorithm;

}
