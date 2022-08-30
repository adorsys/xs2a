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

import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.error.TppMessage;
import lombok.Value;
import org.apache.commons.collections4.CollectionUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@Value
public class SpiResponse<T> {
    private static final VoidResponse VOID_RESPONSE = new VoidResponse();

    /**
     * Business object that is returned in scope of request
     */
    private T payload;

    /**
     * Optional messages that can be returned to explain an error in details. XS2A Service will use it to
     * provide the error explanation to TPP
     */
    @NotNull
    private final List<TppMessage> errors = new ArrayList<>();

    private SpiResponse(SpiResponseBuilder<T> builder) {
        this.payload = builder.payload;
        this.errors.addAll(builder.errors);
    }

    public static VoidResponse voidResponse() {
        return VOID_RESPONSE;
    }

    public static <T> SpiResponseBuilder<T> builder() {
        return new SpiResponseBuilder<>();
    }

    public boolean hasError() {
        return !errors.isEmpty() || payload == null;
    }

    public boolean isSuccessful() {
        return errors.isEmpty()  && payload != null;
    }

    public static class SpiResponseBuilder<T> {
        private T payload;
        private List<TppMessage> errors = new ArrayList<>();

        private SpiResponseBuilder() {
        }

        public SpiResponseBuilder<T> payload(T payload) {
            this.payload = payload;
            return this;
        }

        public SpiResponseBuilder<T> error(@NotNull TppMessage error) {
            this.errors.add(error);
            return this;
        }

        public SpiResponseBuilder<T> error(List<TppMessage> errors) {
            if (CollectionUtils.isNotEmpty(errors)) {
                this.errors.addAll(errors);
            }
            return this;
        }

        public SpiResponse<T> build() {

            if (payload == null && CollectionUtils.isEmpty(errors)) {
                this.error(new TppMessage(MessageErrorCode.INTERNAL_SERVER_ERROR));
            }

            return new SpiResponse<>(this);
        }
    }

    /**
     * To be used for SpiResponse Void case
     */
    public static class VoidResponse {
        private VoidResponse() {
        }
    }
}
