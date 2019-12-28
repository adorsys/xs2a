package de.adorsys.psd2.xs2a.web.error;

import de.adorsys.psd2.mapper.Xs2aObjectMapper;
import de.adorsys.psd2.xs2a.core.domain.MessageCategory;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.mapper.ServiceType;
import de.adorsys.psd2.xs2a.service.discovery.ServiceTypeDiscoveryService;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorMapperContainer;
import de.adorsys.psd2.xs2a.web.filter.TppErrorMessage;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TppErrorMessageWriterTest {
    @Mock
    private ServiceTypeDiscoveryService serviceTypeDiscoveryService;
    @Mock
    private ErrorMapperContainer errorMapperContainer;
    @Mock
    private Xs2aObjectMapper xs2aObjectMapper;
    @Mock
    private HttpServletResponse response;

    private TppErrorMessageWriter tppErrorMessageWriter;

    private final static ServiceType SERVICE_TYPE = ServiceType.AIS;
    private final static MessageCategory MESSAGE_CATEGORY = MessageCategory.ERROR;
    private final static MessageErrorCode MESSAGE_ERROR_CODE = MessageErrorCode.FORMAT_ERROR;
    private final static int STATUS_CODE = HttpServletResponse.SC_UNAUTHORIZED;
    private final static String MESSAGE_ERROR_STRING = "Test";

    private final static PrintWriter PRINT_WRITER = new PrintWriter(System.out);
    private final static ErrorMapperContainer.ErrorBody ERROR_BODY = new ErrorMapperContainer.ErrorBody(new Object(), HttpStatus.OK);

    @Before
    public void setUp() {
        tppErrorMessageWriter = new TppErrorMessageWriter(serviceTypeDiscoveryService, errorMapperContainer, xs2aObjectMapper);
    }

    @Test(expected = IllegalArgumentException.class)
    public void writeError_wrongServiceType() throws IOException {
        TppErrorMessage tppErrorMessage = new TppErrorMessage(MESSAGE_CATEGORY, MESSAGE_ERROR_CODE, MESSAGE_ERROR_STRING);

        tppErrorMessageWriter.writeError(response, STATUS_CODE, tppErrorMessage);

        verify(serviceTypeDiscoveryService).getServiceType();
    }

    @Test
    public void writeError_successful() throws IOException {
        when(serviceTypeDiscoveryService.getServiceType()).thenReturn(SERVICE_TYPE);

        TppErrorMessage tppErrorMessage = new TppErrorMessage(MESSAGE_CATEGORY, MESSAGE_ERROR_CODE, MESSAGE_ERROR_STRING);
        MessageError messageError = new MessageError(ErrorType.AIS_400, TppMessageInformation.of(tppErrorMessage.getCategory(), tppErrorMessage.getCode()));
        when(errorMapperContainer.getErrorBody(messageError)).thenReturn(ERROR_BODY);
        when(response.getWriter()).thenReturn(PRINT_WRITER);

        tppErrorMessageWriter.writeError(response, STATUS_CODE, tppErrorMessage);

        ArgumentCaptor<Writer> writerArgumentCaptor = ArgumentCaptor.forClass(Writer.class);
        ArgumentCaptor<ErrorMapperContainer.ErrorBody> errorBodyArgumentCaptor = ArgumentCaptor.forClass(ErrorMapperContainer.ErrorBody.class);

        verify(serviceTypeDiscoveryService).getServiceType();
        verify(errorMapperContainer).getErrorBody(messageError);
        verify(xs2aObjectMapper).writeValue(writerArgumentCaptor.capture(), errorBodyArgumentCaptor.capture());

        assertEquals(writerArgumentCaptor.getValue(), PRINT_WRITER);
        assertEquals(errorBodyArgumentCaptor.getValue(), ERROR_BODY.getBody());
    }
}
