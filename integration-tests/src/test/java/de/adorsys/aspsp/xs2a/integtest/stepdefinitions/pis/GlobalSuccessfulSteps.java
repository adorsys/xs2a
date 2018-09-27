package de.adorsys.aspsp.xs2a.integtest.stepdefinitions.pis;

import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import de.adorsys.aspsp.xs2a.integtest.config.AuthConfigProperty;
import de.adorsys.aspsp.xs2a.integtest.util.Context;
import de.adorsys.psd2.model.PaymentInitationRequestResponse201;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Objects;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@Slf4j
@FeatureFileSteps
public class GlobalSuccessfulSteps {
    @Autowired
    private Context context;

    @Autowired
    @Qualifier("aspsp-mock")
    private RestTemplate template;

    @Autowired
    private AuthConfigProperty authConfigProperty;

    @Before
    public void loadTestDataIntoDb() {
        template.exchange(
            context.getMockUrl() + "/integration-tests/refresh-testing-data",
            HttpMethod.GET,
            HttpEntity.EMPTY,
            String.class);
    }

    @Given("^PSU request access token for oauth approach$")
    public void requestAccessToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", authConfigProperty.getGrantType());
        map.add("username", authConfigProperty.getUsername());
        map.add("password", authConfigProperty.getPassword());
        map.add("client_id", authConfigProperty.getClientId());
        map.add("client_secret", authConfigProperty.getClientSecret());

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);

        ResponseEntity<HashMap> response = null;
        try {
            response = template.exchange(authConfigProperty.getUrl(), HttpMethod.POST, entity, HashMap.class);
        } catch (RestClientException e) {
            e.printStackTrace();
        }

        context.setScaApproach("oauth");
        context.setAccessToken(Objects.requireNonNull(response).getBody().get("access_token").toString());
    }

    @And("^a redirect URL is delivered to the PSU$")
    public void checkRedirectUrl() {
        ResponseEntity<PaymentInitationRequestResponse201> actualResponse = context.getActualResponse();

        assertThat(actualResponse.getBody().getLinks().get("scaRedirect"), notNullValue());
    }

    @Then("^a successful response code and the appropriate payment response data are received$")
    public void checkResponseCode() {
        ResponseEntity<PaymentInitationRequestResponse201> actualResponse = context.getActualResponse();
        PaymentInitationRequestResponse201 givenResponseBody =  (PaymentInitationRequestResponse201) context.getTestData().getResponse().getBody();

        assertThat(actualResponse.getStatusCode(), equalTo(context.getTestData().getResponse().getHttpStatus()));

        assertThat(actualResponse.getBody().getTransactionStatus(), equalTo(givenResponseBody.getTransactionStatus()));
        assertThat(actualResponse.getBody().getPaymentId(), notNullValue());

        // TODO: Take asserts back in when respective response headers are implemented (https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/289)
        // assertThat(actualResponse.getHeaders().get("Location"), equalTo(context.getBaseUrl() + "/" +
        //    context.getPaymentService() + "/" + actualResponse.getAspspConsentData().getPaymentId()));

        // assertThat(actualResponse.getHeaders().get("X-Request-ID"), equalTo(context.getTestData().getRequest().getHeader().get("x-request-id")));
    }

    @After
    public void afterScenario() {
        log.debug("Cleaning up context");
        context.cleanUp();
    }
}
