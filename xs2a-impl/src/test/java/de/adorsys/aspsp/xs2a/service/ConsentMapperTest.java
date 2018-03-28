package de.adorsys.aspsp.xs2a.service;

import com.google.gson.Gson;
import de.adorsys.aspsp.xs2a.domain.TransactionStatus;
import de.adorsys.aspsp.xs2a.domain.ais.consent.AccountConsent;
import de.adorsys.aspsp.xs2a.domain.ais.consent.CreateConsentReq;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiTransactionStatus;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiCreateConsentRequest;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.nio.charset.Charset;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ConsentMapperTest {
    private final String CREATE_CONSENT_REQ_JSON_PATH = "/json/CreateAccountConsentReqTest.json";
    private final String ACCOUNT_CONSENT_REQ_JSON_PATH = "/json/MapGetAccountConsentTest.json";
    private final Charset UTF_8 = Charset.forName("utf-8");

    @Autowired
    ConsentMapper consentMapper;

    @Test
    public void mapGetAccountConsentStatusById() {
        //Given:
        SpiTransactionStatus expectedTransactionStatus = SpiTransactionStatus.ACCP;

        //When:
        TransactionStatus actualTransactionStatus = consentMapper.mapGetAccountConsentStatusById(expectedTransactionStatus);

        //Then:
        assertThat(expectedTransactionStatus.name()).isEqualTo(actualTransactionStatus.name());
    }

    @Test
    public void mapSpiCreateConsentRequest() throws IOException {
        //Given:
        String aicRequestJson = IOUtils.resourceToString(CREATE_CONSENT_REQ_JSON_PATH, UTF_8);
        CreateConsentReq donorRequest = new Gson().fromJson(aicRequestJson, CreateConsentReq.class);
        SpiCreateConsentRequest expectedRequest = new Gson().fromJson(aicRequestJson, SpiCreateConsentRequest.class);

        //When:
        SpiCreateConsentRequest actualRequest = consentMapper.mapSpiCreateConsentRequest(donorRequest);

        //Then:
        assertThat(actualRequest).isEqualTo(expectedRequest);
    }

    @Test
    public void mapGetAccountConsent() throws IOException{
        //Given:
        String aicRequestJson = IOUtils.resourceToString(ACCOUNT_CONSENT_REQ_JSON_PATH, UTF_8);
        SpiAccountConsent donorConsent = new Gson().fromJson(aicRequestJson, SpiAccountConsent.class);
        AccountConsent expectedConsent = new Gson().fromJson(aicRequestJson, AccountConsent.class);

        //When:
        AccountConsent actualConsent = consentMapper.mapGetAccountConsent(donorConsent);

        //Then:
        assertThat(actualConsent).isEqualTo(expectedConsent);
    }


}
