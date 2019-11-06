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
