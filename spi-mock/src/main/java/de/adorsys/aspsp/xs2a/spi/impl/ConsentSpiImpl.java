package de.adorsys.aspsp.xs2a.spi.impl;

import de.adorsys.aspsp.xs2a.spi.config.RemoteSpiUrls;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiTransactionStatus;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiCreateConsentRequest;
import de.adorsys.aspsp.xs2a.spi.service.ConsentSpi;
import de.adorsys.aspsp.xs2a.spi.test.data.ConsentMockData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

public class ConsentSpiImpl implements ConsentSpi {

    private final RemoteSpiUrls remoteSpiUrls;

    @Autowired
    public ConsentSpiImpl(RemoteSpiUrls remoteSpiUrls) {
        this.remoteSpiUrls = remoteSpiUrls;
    }

    @Override
    public String createAccountConsents(SpiCreateConsentRequest accountInformationConsentRequest,
                                        boolean withBalance, boolean tppRedirectPreferred, String psuId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        RestTemplate template = new RestTemplate();
        String url = remoteSpiUrls.getUrl("getAllConsents");
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
        builder.queryParam("withBalance", withBalance);
        builder.queryParam("tppRedirectPreferred", tppRedirectPreferred);
        builder.queryParam("psuId", psuId);

        HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity(accountInformationConsentRequest, headers);


        ResponseEntity response = null;
        try {
            response = template.postForEntity(builder.build().encode().toUri(), requestEntity, String.class);
        } catch (HttpClientErrorException e) {
            System.out.println("error received\n" + e.getLocalizedMessage());
        }
        return response == null
               ? null : (String) response.getBody();
    }

    @Override
    public SpiTransactionStatus getAccountConsentStatusById(String consentId) {
        return ConsentMockData.getAccountConsentsStatus(consentId);

    }

    @Override
    public SpiAccountConsent getAccountConsentById(String consentId) {
        return ConsentMockData.getAccountConsent(consentId);
    }

    @Override
    public boolean deleteAccountConsentsById(String consentId) {
        if (ConsentMockData.getAccountConsent(consentId) != null) {
            ConsentMockData.deleteAccountConcent(consentId);
            return true;
        }
        return false;
    }
}
