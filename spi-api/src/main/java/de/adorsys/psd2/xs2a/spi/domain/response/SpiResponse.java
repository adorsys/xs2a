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

package de.adorsys.psd2.xs2a.spi.domain.response;

import de.adorsys.psd2.xs2a.core.consent.AspspConsentData;
import lombok.Value;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static de.adorsys.psd2.xs2a.spi.domain.response.SpiResponseStatus.SUCCESS;

@Value
public class SpiResponse<T> {
    private static final VoidResponse VOID_RESPONSE = new VoidResponse();

    /**
     * Business object that is returned in scope of request
     */
    @NotNull
    private T payload;

    /**
     * Consent data - a binary data that is stored in a consent management system. Is not parsed by XS2A layer. May be
     * used by SPI layer to store state information linked to a workflow. May be encrypted in case of need.
     */
    @NotNull
    private AspspConsentData aspspConsentData;

    /**
     * A status of execution result. Is used to provide correct answer to TPP.
     */
    private SpiResponseStatus responseStatus;

    /**
     * An optional message that can be returned to explain response status in details. XS2A Service may use it to
     * provide the error explanation to TPP
     */
    @NotNull
    private final List<String> messages = new ArrayList<>();

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
     */
    public SpiResponse(@NotNull T payload, @NotNull AspspConsentData aspspConsentData,
                       SpiResponseStatus responseStatus, List<String> messages
                      ) {
        //noinspection ConstantConditions - we cannot be sure that @NotNull annotation will be processed be external developer
        if (payload == null) {
            throw new IllegalArgumentException("payload cannot be null");
        }
        //noinspection ConstantConditions - we cannot be sure that @NotNull annotation will be processed be external developer
        if (aspspConsentData == null) {
            throw new IllegalArgumentException("aspspConsentData cannot be null");
        }
        this.payload = payload;
        this.aspspConsentData = aspspConsentData;
        this.responseStatus = responseStatus;
        if (CollectionUtils.isNotEmpty(messages)) {
            this.messages.addAll(messages);
        }
    }

    public SpiResponse(@NotNull T payload, @NotNull AspspConsentData aspspConsentData) {
        this(payload, aspspConsentData, SUCCESS, null);
    }

    private SpiResponse(SpiResponseBuilder<T> builder) {
        this.payload = builder.payload;
        this.aspspConsentData = builder.aspspConsentData;
        this.responseStatus = builder.responseStatus;
        this.messages.addAll(builder.messages);
    }

    public static VoidResponse voidResponse() {
        return VOID_RESPONSE;
    }

    public static <T> SpiResponseBuilder<T> builder() {
        return new SpiResponseBuilder<>();
    }

    public boolean hasError() {
        return responseStatus != SUCCESS;
    }

    public boolean isSuccessful() {
        return responseStatus == SUCCESS;
    }

    public static class SpiResponseBuilder<T> {
        private T payload;
        private AspspConsentData aspspConsentData;
        private SpiResponseStatus responseStatus = SUCCESS;
        private List<String> messages = new ArrayList<>();

        private SpiResponseBuilder() {
        }

        public SpiResponseBuilder<T> payload(@NotNull T payload) {
            //noinspection ConstantConditions - we cannot be sure that @NotNull annotation will be processed be external developer
            if (payload == null) {
                throw new IllegalArgumentException("payload cannot be null");
            }
            this.payload = payload;
            return this;
        }

        public SpiResponseBuilder<T> aspspConsentData(@NotNull AspspConsentData aspspConsentData) {
            //noinspection ConstantConditions - we cannot be sure that @NotNull annotation will be processed be external developer
            if (aspspConsentData == null) {
                throw new IllegalArgumentException("aspspConsentData cannot be null");
            }
            this.aspspConsentData = aspspConsentData;
            return this;
        }

        public SpiResponseBuilder<T> message(@NotNull String message) {
            if (StringUtils.isNotBlank(message)) {
                this.messages.add(message);
            }
            return this;
        }

        public SpiResponseBuilder<T> message(List<String> messages) {
            if (CollectionUtils.isNotEmpty(messages)) {
                this.messages.addAll(messages);
            }
            return this;
        }

        public SpiResponse<T> success() {
            if (payload == null) {
                throw new IllegalStateException("Response payload cannot be null");
            }
            if (aspspConsentData == null) {
                throw new IllegalStateException("Response aspspConsentData cannot be null");
            }
            this.responseStatus = SUCCESS;
            return new SpiResponse<>(this);
        }

        public SpiResponse<T> fail(@NotNull SpiResponseStatus responseStatus) {
            //noinspection ConstantConditions - we cannot be sure that @NotNull annotation will be processed be external developer
            if (responseStatus == null) {
                throw new IllegalArgumentException("responseStatus cannot be null");
            }
            if (aspspConsentData == null) {
                throw new IllegalStateException("Response aspspConsentData cannot be null");
            }
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
