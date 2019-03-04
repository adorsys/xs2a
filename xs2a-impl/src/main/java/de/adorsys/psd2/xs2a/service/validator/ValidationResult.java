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

package de.adorsys.psd2.xs2a.service.validator;

import de.adorsys.psd2.xs2a.domain.MessageErrorCode;
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
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

    private ValidationResult(boolean valid, @Nullable MessageError messageError) {
        this.valid = valid;
        this.messageError = messageError;
    }

    public boolean isNotValid() {
        return !valid;
    }
}
