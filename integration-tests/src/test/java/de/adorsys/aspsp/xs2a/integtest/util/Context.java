package de.adorsys.aspsp.xs2a.integtest.util;

import de.adorsys.aspsp.xs2a.integtest.model.TestData;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Data
@Component
public class Context<T, U, R> {

    @Value("${xs2a.baseUrl}")
    private String baseUrl;

    @Value("${aspspMock.baseUrl}")
    private String mockUrl;

    private String scaApproach;
    private String paymentProduct;
    private String accessToken;
    private TestData<T, U> testData;
    private ResponseEntity<R> actualResponse;
}
