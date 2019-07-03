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

package de.adorsys.psd2.xs2a.web.filter;

import de.adorsys.psd2.xs2a.domain.MessageErrorCode;
import de.adorsys.psd2.xs2a.exception.MessageCategory;
import lombok.Value;

@Value
public class TppErrorMessage {
    private MessageCategory category;
    private MessageErrorCode code;
    private String text;

    @Override
    public String toString() {
        return "{\n" +
                   "  \"tppMessages\": [\n" +
                   "    {\n" +
                   "      \"category\": \"" + category + "\",\n" +
                   "      \"code\": \"" + code + "\",\n" +
                   "      \"text\": \"" + text + "\"\n" +
                   "    }\n" +
                   "  ]\n" +
                   "}";
    }
}
