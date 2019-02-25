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

package de.adorsys.psd2.xs2a.domain;

import de.adorsys.psd2.xs2a.exception.MessageCategory;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.Size;

import static de.adorsys.psd2.xs2a.exception.MessageCategory.ERROR;

@Data
@EqualsAndHashCode(exclude = "text")
public class TppMessageInformation {
    private MessageCategory category;
    private MessageErrorCode messageErrorCode;
    private String path;
    @Size(max = 512)
    private String text;

    public static TppMessageInformation of(MessageErrorCode messageErrorCode) { //NOPMD
        return of(ERROR, messageErrorCode, null);
    }

    public static TppMessageInformation of(MessageErrorCode messageErrorCode, String text) { //NOPMD
        return of(ERROR, messageErrorCode, text);
    }

    public static TppMessageInformation of(MessageCategory category, MessageErrorCode messageErrorCode) { //NOPMD
        return of(category, messageErrorCode, null, null);
    }

    public static TppMessageInformation of(MessageCategory category, MessageErrorCode messageErrorCode, String text) { //NOPMD
        return of(category, messageErrorCode, text, null);
    }

    public static TppMessageInformation of(MessageCategory category, MessageErrorCode messageErrorCode, String text, String path) { //NOPMD
        return new TppMessageInformation(category, messageErrorCode, text, path);
    }

    private TppMessageInformation(MessageCategory category, MessageErrorCode messageErrorCode, String text, String path) {
        this.category = category;
        this.messageErrorCode = messageErrorCode;
        this.text = text;
        this.path = path;
    }
}
