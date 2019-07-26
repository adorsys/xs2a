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

package de.adorsys.psd2.xs2a.spi.domain.response;

import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.error.TppMessage;
import lombok.Value;
import org.apache.commons.collections4.CollectionUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static de.adorsys.psd2.xs2a.spi.domain.response.SpiResponseStatus.LOGICAL_FAILURE;
import static de.adorsys.psd2.xs2a.spi.domain.response.SpiResponseStatus.SUCCESS;
import static java.util.stream.Collectors.toList;

@Value
public class SpiResponse<T> {
    private static final VoidResponse VOID_RESPONSE = new VoidResponse();

    /**
     * Business object that is returned in scope of request
     */
    private T payload;

    /**
     * A status of execution result. Is used to provide correct answer to TPP.
     *
     * @deprecated since 3.5. Use MessageErrorCode in errors list instead.
     */
    @Deprecated //TODO remove not earlier that 3.8 https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/965
    private SpiResponseStatus responseStatus;

    /**
     * Optional messages that can be returned to explain an error in details. XS2A Service will use it to
     * provide the error explanation to TPP
     */
    @NotNull
    private final List<TppMessage> errors = new ArrayList<>();

    private SpiResponse(SpiResponseBuilder<T> builder) {
        this.payload = builder.payload;
        this.responseStatus = builder.responseStatus;
        this.errors.addAll(builder.errors);
    }

    public static VoidResponse voidResponse() {
        return VOID_RESPONSE;
    }

    public static <T> SpiResponseBuilder<T> builder() {
        return new SpiResponseBuilder<>();
    }

    public boolean hasError() {
        return !errors.isEmpty() || responseStatus != SUCCESS || payload == null;
    }

    public boolean isSuccessful() {
        return errors.isEmpty() && responseStatus == SUCCESS && payload != null;
    }

    /**
     * @return List of error messages
     * @deprecated since 3.5
     */
    @Deprecated //TODO remove not earlier that 3.8 https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/965
    public List<String> getMessages() {
        return errors.stream()
                   .map(TppMessage::getMessageText)
                   .collect(toList());
    }

    @NotNull
    //TODO remove with messages removal https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/965
    private static MessageErrorCode getErrorCodeByStatus(SpiResponseStatus responseStatus) {
        MessageErrorCode messageErrorCode = MessageErrorCode.FORMAT_ERROR;
        if (responseStatus != null) {
            switch (responseStatus) {
                case NOT_SUPPORTED:
                    messageErrorCode = MessageErrorCode.PARAMETER_NOT_SUPPORTED;
                    break;
                case TECHNICAL_FAILURE:
                    messageErrorCode = MessageErrorCode.INTERNAL_SERVER_ERROR;
                    break;
                case UNAUTHORIZED_FAILURE:
                    messageErrorCode = MessageErrorCode.PSU_CREDENTIALS_INVALID;
                    break;
                case LOGICAL_FAILURE:
                default:
                    messageErrorCode = MessageErrorCode.FORMAT_ERROR;
            }
        }
        return messageErrorCode;
    }

    public static class SpiResponseBuilder<T> {
        private T payload;
        private SpiResponseStatus responseStatus;
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
            if (this.responseStatus == null) {
                this.responseStatus = this.errors.isEmpty() ? SUCCESS : LOGICAL_FAILURE;
            }

            if (payload == null && CollectionUtils.isEmpty(errors)) {
                this.error(new TppMessage(MessageErrorCode.INTERNAL_SERVER_ERROR, ""));
            }

            return new SpiResponse<>(this);
        }

        /**
         * @param responseStatus deprecated
         * @return SpiResponse object
         * @see #build()
         * @deprecated since 3.5. Use build instead
         */
        @Deprecated //TODO remove not earlier that 3.8 https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/965
        public SpiResponse<T> fail(@Deprecated @NotNull SpiResponseStatus responseStatus) {
            this.responseStatus = responseStatus;
            this.errors.add(new TppMessage(getErrorCodeByStatus(responseStatus), ""));
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
