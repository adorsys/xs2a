package de.adorsys.aspsp.xs2a.integtest.stepdefinitions.pis;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import de.adorsys.aspsp.xs2a.integtest.config.AuthConfigProperty;
import de.adorsys.aspsp.xs2a.integtest.entities.ITMessageError;
import de.adorsys.aspsp.xs2a.integtest.util.Context;
import de.adorsys.psd2.model.PaymentInitationRequestResponse201;
import de.adorsys.psd2.model.TppMessageGeneric;
import de.adorsys.psd2.model.TppMessages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@FeatureFileSteps
public class GlobalSteps {
    @Autowired
    private Context context;

    @Autowired
    @Qualifier("aspsp-mock")
    private RestTemplate template;

    @Autowired
    private AuthConfigProperty authConfigProperty;

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

    @Then("^an error response code is displayed the appropriate error response$")
    public void anErrorResponseCodeIsDisplayedTheAppropriateErrorResponse() {
        ITMessageError actualErrorObject = context.getMessageError();
        PaymentInitationRequestResponse201 givenResponseBody = (PaymentInitationRequestResponse201) context.getTestData().getResponse().getBody();

        HttpStatus httpStatus = context.getTestData().getResponse().getHttpStatus();
        assertThat(context.getActualResponse().getStatusCode(), equalTo(httpStatus));
        assertThat(actualErrorObject.getTransactionStatus().toString(), equalTo(givenResponseBody.getTransactionStatus().toString()));

        TppMessages givenTppMessages = givenResponseBody.getTppMessages();

        Set<TppMessageGeneric> actualTppMessages = actualErrorObject.getTppMessages();

        assertThat(actualTppMessages, is(equalTo(givenTppMessages)));

        actualTppMessages.forEach ((msg) -> {
            assertThat(msg.getCategory().toString(), equalTo(givenTppMessages.get(msg.getCategory().ordinal()).getCategory().toString()));
            assertThat(msg.getCode().toString(), equalTo(givenTppMessages.get(msg.getCategory().ordinal()).getCode().toString()));
        });
    }
}
