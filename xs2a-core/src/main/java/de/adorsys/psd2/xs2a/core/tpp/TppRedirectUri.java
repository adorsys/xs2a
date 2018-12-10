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

package de.adorsys.psd2.xs2a.core.tpp;

import io.swagger.annotations.ApiModelProperty;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * TPP redirect URIs, used in Redirect SCA approach.
 */
@Value
public class TppRedirectUri {
    @NotNull
    @ApiModelProperty(value = "Redirect URI", example = "Redirect URI", required = true)
    private final String uri;

    @Nullable
    @ApiModelProperty(value = "Nok redirect URI", example = "Nok redirect URI")
    private String nokUri;
}
