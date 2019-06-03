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

import de.adorsys.psd2.xs2a.core.consent.AspspConsentData;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.error.TppMessage;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import lombok.Value;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
     * Consent data - a binary data that is stored in a consent management system. Is not parsed by XS2A layer. May be
     * used by SPI layer to store state information linked to a workflow. May be encrypted in case of need.
     *
     * @deprecated since 3.0. Use SpiAspspConsentDataProvider instead.
     * @see SpiAspspConsentDataProvider
     * // TODO remove aspspConsentData from SPI Response in version 3.4 or later https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/786
     */
    @Nullable
    @Deprecated
    private AspspConsentData aspspConsentData;

    /**
     * A status of execution result. Is used to provide correct answer to TPP.
     * @deprecated since 3.5. Use MessageErrorCode in errors list instead.
     */
    @Deprecated //TODO remove not earlier that 3.8 https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/392
    private SpiResponseStatus responseStatus;

    /**
     * Optional messages that can be returned to explain an error in details. XS2A Service will use it to
     * provide the error explanation to TPP
     */
    @NotNull
    private final List<TppMessage> errors = new ArrayList<>();

    /**
     * @param payload
     *         - Payload to be returned. Cannot be null. If you need to return VoidResponse use {@link #voidResponse()}
     * @param aspspConsentData
     *         - AspspConsentData to be returned. Cannot be null. Use requests aspspConsentData {@link
     *         AspspConsentData#respondWith(byte[])} method.
     * @param responseStatus
     *         - Status of the processing call. Defaults to error.
     * @param messages
     *         - Optional messages to be provided to the TPP.
     * @deprecated since 3.5. Use Builder instead
     * @see #builder()
     */
    @Deprecated //TODO remove not earlier that 3.8 https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/392
    public SpiResponse(T payload, @Nullable @Deprecated AspspConsentData aspspConsentData,
                       SpiResponseStatus responseStatus, List<String> messages
                      ) {
        if (responseStatus == SUCCESS && payload == null) {
            throw new IllegalArgumentException("Payload must be filled by successful result");
        }
        this.payload = payload;
        this.aspspConsentData = aspspConsentData;
        this.responseStatus = responseStatus;
        if (CollectionUtils.isNotEmpty(messages)) {
            this.errors.addAll(messagesToErrors(this.responseStatus, messages));
        }
    }

    /**
     * @param payload reposnse payload
     * @param aspspConsentData deprecated
     * @deprecated since 3.5. Use Builder instead
     * @see #builder()
     */
    @Deprecated //TODO remove not earlier that 3.8 https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/392
    public SpiResponse(@NotNull T payload, @Nullable @Deprecated AspspConsentData aspspConsentData) {
        this(payload, aspspConsentData, SUCCESS, null);
    }

    private SpiResponse(SpiResponseBuilder<T> builder) {
        this.payload = builder.payload;
        this.aspspConsentData = builder.aspspConsentData;
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
        return !errors.isEmpty() || responseStatus != SUCCESS|| payload == null;
    }

    public boolean isSuccessful() {
        return errors.isEmpty() && responseStatus == SUCCESS && payload != null;
    }

    /**
     * @return List of error messages
     * @deprecated since 3.5
     */
    @Deprecated //TODO remove not earlier that 3.8 https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/392
    public List<String> getMessages() {
        return errors.stream()
            .map(TppMessage::getMessageText)
            .collect(toList());
    }

    //TODO remove with messages removal https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/392
    private static List<TppMessage> messagesToErrors(SpiResponseStatus responseStatus, Collection<String> messages) {
        if (CollectionUtils.isEmpty(messages)) {
            return Collections.emptyList();
        }
        MessageErrorCode messageErrorCode = getErrorCodeByStatus(responseStatus);

        return messages.stream()
                .map(m -> new TppMessage(messageErrorCode, m))
                .collect(toList());
    }

    @NotNull
    //TODO remove with messages removal https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/392
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
        private AspspConsentData aspspConsentData;
        private SpiResponseStatus responseStatus;
        private List<TppMessage> errors = new ArrayList<>();

        private SpiResponseBuilder() {
        }

        public SpiResponseBuilder<T> payload(T payload) {
            this.payload = payload;
            return this;
        }

        /**
         * @deprecated since 3.0. Use SpiAspspConsentDataProvider instead.
         * @see SpiAspspConsentDataProvider
         * @param aspspConsentData aspspConsentData
         * @return SpiResponseBuilder
         */
        @Deprecated
        public SpiResponseBuilder<T> aspspConsentData(@Nullable AspspConsentData aspspConsentData) {
            this.aspspConsentData = aspspConsentData;
            return this;
        }

        /**
         * @param message message to add
         * @return SpiResponseBuilder
         * @deprecated since 3.5. Use error instead
         * @see #error(TppMessage)
         */
        @Deprecated //TODO remove not earlier that 3.8 https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/392
        public SpiResponseBuilder<T> message(@NotNull String message) {
            MessageErrorCode messageErrorCode = getErrorCodeByStatus(this.responseStatus);
            if (StringUtils.isNotBlank(message)) {
                this.errors.add(new TppMessage(messageErrorCode, message, null));
            }
            return this;
        }

        /**
         * @param messages messages to add
         * @return SpiResponseBuilder
         * @deprecated since 3.5. Use error instead
         * @see #error(List)
         */
        @Deprecated //TODO remove not earlier that 3.8 https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/392
        public SpiResponseBuilder<T> message(List<String> messages) {
            if (CollectionUtils.isNotEmpty(messages)) {
                this.errors.addAll(messagesToErrors(this.responseStatus, messages));
            }
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
            return new SpiResponse<>(this);
        }

        /**
         * @return SpiResponse object
         * @deprecated since 3.5. Use build instead
         * @see #build()
         */
        @Deprecated //TODO remove not earlier that 3.8 https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/392
        public SpiResponse<T> success() {
            if (payload == null) {
                throw new IllegalStateException("Response payload cannot be null");
            }
            this.responseStatus = SUCCESS;
            return new SpiResponse<>(this);
        }

        /**
         * @param responseStatus deprecated
         * @return SpiResponse object
         * @deprecated since 3.5. Use build instead
         * @see #build()
         */
        @Deprecated //TODO remove not earlier that 3.8 https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/392
        public SpiResponse<T> fail(@Deprecated @NotNull SpiResponseStatus responseStatus) {
            this.responseStatus = responseStatus;
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
