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

package de.adorsys.psd2.xs2a.core.service.validator;

import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value
public class ValidationResult {
    private boolean valid;

    /**
     * Could be null for valid request (valid == true case)
     */
    @Nullable
    private MessageError messageError;

    public static ValidationResult valid() {
        return new ValidationResult(true, null);
    }

    public static ValidationResult invalid(@NotNull MessageError messageError) {
        return new ValidationResult(false, messageError);
    }

    public static ValidationResult invalid(@NotNull ErrorType errorType, MessageErrorCode messageErrorCode) {
        return new ValidationResult(false, new MessageError(errorType, TppMessageInformation.of(messageErrorCode)));
    }

    public static ValidationResult invalid(@NotNull ErrorType errorType, TppMessageInformation tppMessageInformation) {
        return new ValidationResult(false, new MessageError(errorType, tppMessageInformation));
    }

    public static ValidationResult invalid(@NotNull ErrorType errorType, TppMessageInformation... tppMessageInformations) {
        return new ValidationResult(false, new MessageError(errorType, tppMessageInformations));
    }

    private ValidationResult(boolean valid, @Nullable MessageError messageError) {
        this.valid = valid;
        this.messageError = messageError;
    }

    public boolean isNotValid() {
        return !valid;
    }
}
