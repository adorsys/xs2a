package de.adorsys.aspsp.xs2a.spi.domain;

import de.adorsys.aspsp.xs2a.spi.domain.consent.AspspConsentData;
import lombok.Value;

import static de.adorsys.aspsp.xs2a.spi.domain.SpiResponseStatus.SUCCESS;

@Value
public class SpiResponse<T> {
    private T payload;
    private AspspConsentData aspspConsentData;
    private SpiResponseStatus responseStatus;
    private String message;

    public SpiResponse(T payload, AspspConsentData aspspConsentData, SpiResponseStatus responseStatus, String message) {
        this.payload = payload;
        this.aspspConsentData = aspspConsentData;
        this.responseStatus = responseStatus;
        this.message = message;
    }

    public SpiResponse(T payload, AspspConsentData aspspConsentData) {
        this(payload, aspspConsentData, null, null);
    }

    public boolean hasError() {
        return responseStatus != SUCCESS;
    }

    public boolean isSuccessful() {
        return responseStatus == SUCCESS;
    }

    private SpiResponse(SpiResponseBuilder<T> builder) {
        this.payload = builder.payload;
        this.aspspConsentData = builder.aspspConsentData;
        this.responseStatus = builder.responseStatus;
        this.message = builder.message;
    }

    public static <T> SpiResponseBuilder<T> builder() {
        return new SpiResponseBuilder<>();
    }

    public static class SpiResponseBuilder<T> {
        private T payload;
        private AspspConsentData aspspConsentData;
        private SpiResponseStatus responseStatus;
        private String message;

        public SpiResponseBuilder<T> success(T payload, AspspConsentData aspspConsentData) {
            this.payload = payload;
            this.aspspConsentData = aspspConsentData;
            this.responseStatus = SUCCESS;
            return this;

        }

        public SpiResponseBuilder<T> success() {
            return success(null,null);
        }

        public SpiResponseBuilder<T> fail(T payload, AspspConsentData aspspConsentData, SpiResponseStatus responseStatus, String message) {
            this.payload = payload;
            this.aspspConsentData = aspspConsentData;
            this.responseStatus = responseStatus;
            this.message = message;
            return this;
        }

        public SpiResponseBuilder<T> fail(T payload, AspspConsentData aspspConsentData, SpiResponseStatus responseStatus) {
            return fail(payload, aspspConsentData, responseStatus, null);
        }

        public SpiResponseBuilder<T> fail(SpiResponseStatus responseStatus, String message) {
            return fail(null, null, responseStatus, message);

        }

        public SpiResponseBuilder<T> fail(SpiResponseStatus responseStatus) {
            return fail(null, null, responseStatus, null);
        }

        public SpiResponse<T> build() {
            return new SpiResponse<>(this);
        }
    }
}
