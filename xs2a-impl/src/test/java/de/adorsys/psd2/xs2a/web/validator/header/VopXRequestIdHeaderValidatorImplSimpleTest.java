package de.adorsys.psd2.xs2a.web.validator.header;

import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class VopXRequestIdHeaderValidatorImplSimpleTest {

    private static final String X_REQUEST_ID_HEADER = UUID.randomUUID().toString();
    private static final String[] X_REQUEST_ID_HEADER_NAME = {"x-request-id"};

    private VopXRequestIdHeaderValidatorImpl validator;
    private MessageError messageError;
    private Map<String, String> headers;

    @BeforeEach
    void setUp() {
        validator = new VopXRequestIdHeaderValidatorImpl(new ErrorBuildingServiceMock(ErrorType.AIS_400));
        messageError = new MessageError(ErrorType.AIS_400);
        headers = new HashMap<>();
    }

    @Test
    void validate_validUuid_shouldPass() {
        headers.put(validator.getHeaderName(), X_REQUEST_ID_HEADER);
        validator.validate(headers, messageError);
        assertTrue(messageError.getTppMessages().isEmpty());
    }

    @Test
    void validate_invalidUuid_shouldFail() {
        headers.put(validator.getHeaderName(), "invalid-uuid");
        validator.validate(headers, messageError);

        assertEquals(MessageErrorCode.FORMAT_ERROR_WRONG_HEADER, messageError.getTppMessage().getMessageErrorCode());
    }

    @Test
    void validate_missingHeader_shouldFail() {
        validator.validate(headers, messageError);

        assertEquals(MessageErrorCode.FORMAT_ERROR_ABSENT_HEADER, messageError.getTppMessage().getMessageErrorCode());
        assertArrayEquals(X_REQUEST_ID_HEADER_NAME, messageError.getTppMessage().getTextParameters());
    }

    @Test
    void validate_emptyHeader_shouldFail() {
        headers.put(validator.getHeaderName(), "");
        validator.validate(headers, messageError);

        assertEquals(MessageErrorCode.FORMAT_ERROR_BLANK_HEADER, messageError.getTppMessage().getMessageErrorCode());
        assertArrayEquals(X_REQUEST_ID_HEADER_NAME, messageError.getTppMessage().getTextParameters());
    }
}