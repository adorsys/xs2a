package de.adorsys.aspsp.aspspmockserver.web;

import de.adorsys.aspsp.aspspmockserver.service.ConsentService;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiAccountAccess;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiConsentStatus;
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
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ConsentControllerTest {
    private final String CORRECT_PSU_ID = "123456789";
    private final String WRONG_PSU_ID = "wrong psu id";
    private final SpiAccountConsent CONSENT = createConsent(CORRECT_PSU_ID);
    private final SpiAccountConsent CONSENT_WRONG = createConsent(WRONG_PSU_ID);

    @Autowired
    private ConsentController consentController;

    @MockBean(name = "consentService")
    private ConsentService consentService;

    @Before
    public void setUp() {
        when(consentService.createConsentAndReturnId(CONSENT)).thenReturn(Optional.of(CORRECT_PSU_ID));
        when(consentService.createConsentAndReturnId(CONSENT_WRONG)).thenReturn(Optional.empty());
        when(consentService.getConsent(CORRECT_PSU_ID)).thenReturn(Optional.of(CONSENT));
        when(consentService.getConsent(WRONG_PSU_ID)).thenReturn(Optional.empty());
        when(consentService.deleteConsentById(CORRECT_PSU_ID)).thenReturn(true);
        when(consentService.deleteConsentById(WRONG_PSU_ID)).thenReturn(false);
    }

    @Test
    public void createAccountConsentTest_Success() {
        //When:
        ResponseEntity expectedResponse = consentController.createConsent(CONSENT);

        //Then:
        assertThat(expectedResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    public void createAccountConsentTest_Failure() {
        //When:
        ResponseEntity expectedResponse = consentController.createConsent(CONSENT_WRONG);

        //Then:
        assertThat(expectedResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void readConsentByIdTest_Success(){
        //When:
        ResponseEntity expectedResponse = consentController.readConsentById(CORRECT_PSU_ID);

        //Then:
        assertThat(expectedResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(expectedResponse.getBody()).isEqualTo(CONSENT);
    }

    @Test
    public void readConsentByIdTest_Failure(){
        //When:
        ResponseEntity expectedResponse = consentController.readConsentById(WRONG_PSU_ID);

        //Then:
        assertThat(expectedResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(expectedResponse.getBody()).isEqualTo(null);
    }

    @Test
    public void deleteConsentTest_Success(){
        //When:
        ResponseEntity expectedResponse = consentController.deleteConsent(CORRECT_PSU_ID);

        //Then:
        assertThat(expectedResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    public void deleteConsentTest_Failure(){
        //When:
        ResponseEntity expectedResponse = consentController.deleteConsent(WRONG_PSU_ID);

        //Then:
        assertThat(expectedResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    private SpiAccountConsent createConsent(String consentId){
        return new SpiAccountConsent(
            consentId,
            new SpiAccountAccess(),
            false, new Date(), 4, new Date(), SpiConsentStatus.VALID,true,false);
    }
}
