/*
 * Copyright 2018-2021 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.exception.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.adorsys.psd2.xs2a.domain.HrefType;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * NextGenPSD2 specific definition of reporting error information in case of a HTTP error code 415, 429, 500.
 */
@Data
@NoArgsConstructor
@SuperBuilder
@ApiModel(description = "NextGenPSD2 specific definition of reporting error information in case of a HTTP error code 415, 429, 500. ")
@Validated
public class AbstractErrorNGAIS<T> {
    @JsonProperty("tppMessages")
    @Valid
    private List<T> tppMessages;

    @JsonProperty("links")
    private Map<String, HrefType> links;
}
