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

package de.adorsys.psd2.consent.api;

import lombok.Value;
import org.jetbrains.annotations.NotNull;

@Value
public class CmsResponse<T> {
    private static final VoidResponse VOID_RESPONSE = new VoidResponse();

    /**
     * Business object that is returned in scope of request
     */
    private T payload;

    /**
     * Optional messages that can be returned to explain an error in details.
     * So far, XS2A Service doesn't check for a specific error, but rather for presence or absence of business object
     */
    private CmsError error;

    private CmsResponse(CmsResponseBuilder<T> builder) {
        this.payload = builder.payload;
        this.error = builder.error;
    }

    public static VoidResponse voidResponse() {
        return VOID_RESPONSE;
    }

    public static <T> CmsResponseBuilder<T> builder() {
        return new CmsResponseBuilder<>();
    }

    public boolean hasError() {
        return error != null || payload == null;
    }

    public boolean isSuccessful() {
        return error == null && payload != null;
    }

    public static class CmsResponseBuilder<T> {
        private T payload;
        private CmsError error;

        private CmsResponseBuilder() {
        }

        public CmsResponseBuilder<T> payload(T payload) {
            this.payload = payload;
            return this;
        }

        public CmsResponseBuilder<T> error(@NotNull CmsError error) {
            this.error = error;
            return this;
        }

        public CmsResponse<T> build() {

            if (payload == null && error == null) {
                this.error = CmsError.TECHNICAL_ERROR;
            }

            return new CmsResponse<>(this);
        }
    }

    public static class VoidResponse {
        private VoidResponse() {
        }
    }
}
