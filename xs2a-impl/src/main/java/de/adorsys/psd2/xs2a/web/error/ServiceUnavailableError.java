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

package de.adorsys.psd2.xs2a.web.error;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.adorsys.psd2.model.TppMessageCategory;
import lombok.Value;
import org.springframework.http.HttpStatus;

import java.util.Collections;
import java.util.List;

@Value
public class ServiceUnavailableError {
    @JsonProperty("tppMessages")
    private List<ServiceUnavailableTppMessage> tppMessages = Collections.singletonList(new ServiceUnavailableTppMessage());

    @Value
    private static class ServiceUnavailableTppMessage {
        private TppMessageCategory category = TppMessageCategory.WARNING;
        private String code = HttpStatus.SERVICE_UNAVAILABLE.name();
        private String text = HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase();
    }
}
