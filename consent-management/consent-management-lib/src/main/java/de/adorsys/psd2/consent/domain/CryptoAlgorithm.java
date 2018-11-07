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

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Entity(name = "crypto_algorithm")
@ApiModel(description = "Crypto Algorithm", value = "CryptoAlgorithm")
@NoArgsConstructor
public class CryptoAlgorithm {
    @Id
    @Column(name = "algorithm_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "crypto_algorithm_generator")
    @SequenceGenerator(name = "crypto_algorithm_generator", sequenceName = "crypto_algorithm_id_seq")
    private Long id;

    @ApiModelProperty(value = "External Id", required = true, example = "nML0IXWdMa")
    @Column(name = "external_id", nullable = false, unique = true)
    private String externalId;

    @ApiModelProperty(value = "Algorithm", required = true, example = "AES/GCM/NoPadding")
    @Column(name = "algorithm", nullable = false)
    private String algorithm;

    @ApiModelProperty(value = "Version", required = true, example = "1")
    @Column(name = "version", nullable = false)
    private String version;
}
