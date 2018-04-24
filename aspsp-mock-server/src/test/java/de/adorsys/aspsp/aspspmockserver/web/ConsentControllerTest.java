package de.adorsys.aspsp.aspspmockserver.web;

import de.adorsys.aspsp.aspspmockserver.service.ConsentService;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiAccountAccess;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiCreateConsentRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ConsentControllerTest {
    private final String CORRECT_PSU_ID = "123456789";
    private final String WRONG_PSU_ID = "wrong psu id";

    @Autowired
    private ConsentController consentController;

    @MockBean(name = "consentService")
    private ConsentService consentService;

    @Before
    public void setUp() {
        when(consentService.createConsentAndReturnId(any(), eq(CORRECT_PSU_ID))).thenReturn(Optional.of("someString"));
        when(consentService.createConsentAndReturnId(any(), eq(WRONG_PSU_ID))).thenReturn(Optional.empty());
    }

    @Test
    public void createAccountConsentTest_Success() {
        //When:
        ResponseEntity expectedResponse = consentController.createConsent(createConsentRequest(), CORRECT_PSU_ID);

        //Then:
        assertThat(expectedResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    public void createAccountConsentTest_Failure() {
        //When:
        ResponseEntity expectedResponse = consentController.createConsent(createConsentRequest(), WRONG_PSU_ID);

        //Then:
        assertThat(expectedResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    private SpiCreateConsentRequest createConsentRequest() {
        return new SpiCreateConsentRequest(new SpiAccountAccess(null, null, null, null, null), true, new Date(), 4, false);
    }
}
