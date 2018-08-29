package de.adorsys.aspsp.xs2a.integtest.util;

import de.adorsys.aspsp.xs2a.integtest.model.TestData;
import de.adorsys.psd2.model.TppMessages;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Data
@Component
public class Context<T, U> {

    @Value("${xs2a.baseUrl}")
    private String baseUrl;

    @Value("${aspspMock.baseUrl}")
    private String mockUrl;

    private String scaApproach;
    private String paymentProduct;
    private String paymentService;
    private String accessToken;
    private String paymentId;
    private TestData<T, U> testData;
    private ResponseEntity<U> actualResponse;
    private TppMessages Tppmessage;
    private HttpStatus actualResponseStatus;
}
