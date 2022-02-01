/*
 * Copyright 2018-2022 adorsys GmbH & Co KG
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version. This program is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 *
 * This project is also available under a separate commercial license. You can
 * contact us at psd2@adorsys.com.
 */

package de.adorsys.psd2.xs2a.core.domain;

import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import lombok.Data;
import lombok.EqualsAndHashCode;

import static de.adorsys.psd2.xs2a.core.domain.MessageCategory.ERROR;
import static de.adorsys.psd2.xs2a.core.domain.MessageCategory.WARNING;

@Data
@EqualsAndHashCode
public class TppMessageInformation {
    private MessageCategory category;
    private MessageErrorCode messageErrorCode;
    private String path;
    private Object[] textParameters;
    private String text;

    public static TppMessageInformation buildWithCustomWarning(MessageErrorCode messageErrorCode, String text) {
        return new TppMessageInformation(WARNING, messageErrorCode, null, text, (Object) null);
    }

    public static TppMessageInformation buildWithCustomError(MessageErrorCode messageErrorCode, String text) {
        return new TppMessageInformation(ERROR, messageErrorCode, null, text, (Object) null);
    }

    public static TppMessageInformation buildWarning(String text) {
        return new TppMessageInformation(WARNING, null, null, text);
    }

    public static TppMessageInformation of(MessageErrorCode messageErrorCode) { //NOPMD
        return of(ERROR, messageErrorCode, (Object) null);
    }

    public static TppMessageInformation of(MessageErrorCode messageErrorCode, Object... textParameters) { //NOPMD
        return of(ERROR, messageErrorCode, textParameters);
    }

    public static TppMessageInformation of(MessageCategory category, MessageErrorCode messageErrorCode) { //NOPMD
        return of(category, messageErrorCode, null, (Object) null);
    }

    public static TppMessageInformation of(MessageCategory category, MessageErrorCode messageErrorCode, Object... textParameters) { //NOPMD
        return of(category, messageErrorCode, null, textParameters);
    }

    public static TppMessageInformation of(MessageCategory category, MessageErrorCode messageErrorCode, String path, Object... textParameters) { //NOPMD
        return new TppMessageInformation(category, messageErrorCode, path, null, textParameters);
    }

    private TppMessageInformation(MessageCategory category, MessageErrorCode messageErrorCode, String path, String text, Object... textParameters) {
        this.category = category;
        this.messageErrorCode = messageErrorCode;
        this.path = path;
        this.text = text;
        this.textParameters = textParameters;
    }
}
