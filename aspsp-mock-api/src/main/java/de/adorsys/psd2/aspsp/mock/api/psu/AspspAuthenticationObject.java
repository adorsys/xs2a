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

package de.adorsys.psd2.aspsp.mock.api.psu;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.data.annotation.PersistenceConstructor;

@Data
public class AspspAuthenticationObject {
    private String authenticationType;
    private String authenticationMethodId;
    private String authenticationVersion;
    private String name;
    private String explanation;
    private boolean decoupled;

    public AspspAuthenticationObject(String authenticationType, String authenticationMethodId) {
        this(authenticationType, authenticationMethodId, false);
    }

    @JsonCreator
    @PersistenceConstructor
    public AspspAuthenticationObject(@JsonProperty("authenticationType") String authenticationType, @JsonProperty("authenticationMethodId") String authenticationMethodId, @JsonProperty("decoupled") boolean decoupled) {
        this.authenticationType = authenticationType;
        this.authenticationMethodId = authenticationMethodId;
        this.decoupled = decoupled;
    }
}

