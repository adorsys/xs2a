package de.adorsys.aspsp.xs2a.integtest.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.aspsp.xs2a.integtest.model.TestData;
import de.adorsys.psd2.model.TppMessages;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;

import java.io.IOException;

@Data
@Component
public class Context<T, U> {

    @Value("${xs2a.baseUrl}")
    private String baseUrl;

    @Value("${aspspMock.baseUrl}")
    private String mockUrl;

    @Value("${aspspProfile.baseUrl}")
    private String profileUrl;

    @Autowired
    private ObjectMapper mapper;

    private String scaApproach;
    private String scaMethod;
    private String tanValue;
    private String paymentProduct;
    private String paymentService;
    private String accessToken;
    private String paymentId;
    private String authorisationId;
    private String consentId;
    private String ressourceId;
    private String queryParams;
    private String transactionId;
    private TestData<T, U> testData;
    private ResponseEntity<U> actualResponse;
    private TppMessages tppMessages;
    private HttpStatus actualResponseStatus;

    public void handleRequestError(RestClientResponseException exceptionObject) throws IOException {
        this.setActualResponseStatus(HttpStatus.valueOf(exceptionObject.getRawStatusCode()));
        String responseBodyAsString = exceptionObject.getResponseBodyAsString();
        TppMessages tppMessages = mapper.readValue(responseBodyAsString, TppMessages.class);
        this.setTppMessages(tppMessages);
    }

    public void cleanUp() {
        this.scaApproach = null;
        this.scaMethod = null;
        this.tanValue = null;
        this.paymentProduct = null;
        this.paymentService = null;
        this.accessToken = null;
        this.paymentId = null;
        this.authorisationId = null;
        this.consentId = null;
        this.ressourceId = null;
        this.queryParams = null;
        this.transactionId = null;
        this.testData = null;
        this.actualResponse = null;
        this.tppMessages = null;
        this.actualResponseStatus = null;
    }
}
