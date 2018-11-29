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

package de.adorsys.psd2.xs2a.service.validator.tpp;

import de.adorsys.psd2.xs2a.core.tpp.TppRole;
import lombok.Value;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Value
public class TppRoleAccess {
    private Map<TppRole, Set<String>> tppRoleAccess;

    private TppRoleAccess(TppAccessBuilder builder) {
        this.tppRoleAccess = builder.tppRoleAccess;
    }

    public static TppAccessBuilder builder() {
        return new TppAccessBuilder();
    }

    @Value
    public static class TppAccessBuilder {
        private Map<TppRole, Set<String>> tppRoleAccess = new HashMap<>();

        private TppAccessBuilder() {
        }

        TppAccessBuilder linkTppRolePatterns(TppRole tppRole, String... patterns) {
            Assert.notEmpty(patterns, "Patterns must be set!");

            Set<String> requestMatchers = Arrays.stream(patterns)
                                              .collect(Collectors.toSet());

            if (this.tppRoleAccess.containsKey(tppRole)) {
                this.tppRoleAccess.get(tppRole)
                    .addAll(requestMatchers);
            } else {
                this.tppRoleAccess.put(tppRole, requestMatchers);
            }
            return this;
        }

        public Map<TppRole, Set<String>> build() {
            return new TppRoleAccess(this).getTppRoleAccess();
        }
    }
}
