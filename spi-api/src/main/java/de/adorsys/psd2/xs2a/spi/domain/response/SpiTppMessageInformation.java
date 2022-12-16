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

package de.adorsys.psd2.xs2a.spi.domain.response;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class SpiTppMessageInformation {
    private SpiMessageCategory category;
    private SpiMessageErrorCode messageErrorCode;
    private String path;
    private Object[] textParameters;
    private String text;

    public static SpiTppMessageInformation buildWithCustomWarning(SpiMessageErrorCode messageErrorCode, String text) {
        return new SpiTppMessageInformation(SpiMessageCategory.WARNING, messageErrorCode, null, text, (Object) null);
    }

    public static SpiTppMessageInformation buildWithCustomError(SpiMessageErrorCode messageErrorCode, String text) {
        return new SpiTppMessageInformation(SpiMessageCategory.ERROR, messageErrorCode, null, text, (Object) null);
    }

    public static SpiTppMessageInformation buildWarning(String text) {
        return new SpiTppMessageInformation(SpiMessageCategory.WARNING, null, null, text);
    }

    public static SpiTppMessageInformation of(SpiMessageErrorCode messageErrorCode) { //NOPMD
        return of(SpiMessageCategory.ERROR, messageErrorCode, (Object) null);
    }

    public static SpiTppMessageInformation of(SpiMessageErrorCode messageErrorCode, Object... textParameters) { //NOPMD
        return of(SpiMessageCategory.ERROR, messageErrorCode, textParameters);
    }

    public static SpiTppMessageInformation of(SpiMessageCategory category, SpiMessageErrorCode messageErrorCode) { //NOPMD
        return of(category, messageErrorCode, null, (Object) null);
    }

    public static SpiTppMessageInformation of(SpiMessageCategory category, SpiMessageErrorCode messageErrorCode, Object... textParameters) { //NOPMD
        return of(category, messageErrorCode, null, textParameters);
    }

    public static SpiTppMessageInformation of(SpiMessageCategory category, SpiMessageErrorCode messageErrorCode, String path, Object... textParameters) { //NOPMD
        return new SpiTppMessageInformation(category, messageErrorCode, path, null, textParameters);
    }

    private SpiTppMessageInformation(SpiMessageCategory category, SpiMessageErrorCode messageErrorCode, String path, String text, Object... textParameters) {
        this.category = category;
        this.messageErrorCode = messageErrorCode;
        this.path = path;
        this.text = text;
        this.textParameters = textParameters;
    }
}
