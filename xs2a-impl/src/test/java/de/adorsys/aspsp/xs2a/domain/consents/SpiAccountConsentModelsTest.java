/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.aspsp.xs2a.domain.consents;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.adorsys.aspsp.xs2a.component.JsonConverter;
import de.adorsys.aspsp.xs2a.domain.account.Xs2aAccountReference;
import de.adorsys.aspsp.xs2a.domain.consent.CreateConsentReq;
import de.adorsys.aspsp.xs2a.domain.consent.Xs2aAccountAccess;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.io.IOException;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class SpiAccountConsentModelsTest {
    private static final String CREATE_CONSENT_REQ_JSON_PATH = "/json/CreateAccountConsentReqTest.json";
    private static final String NO_DEDICATE_REQ_PATH = "/json/CreateConsentsNoDedicateAccountReqTest.json";
    private final String CREATE_CONSENT_REQ_WRONG_JSON_PATH = "/json/CreateAccountConsentReqWrongTest.json";
    private static final Charset UTF_8 = Charset.forName("utf-8");
    private Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    private ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private JsonConverter jsonConverter = new JsonConverter(objectMapper);

    @Test
    public void createConsentReq_jsonTest() throws IOException {
        //Given:
        String requestStringJson = IOUtils.resourceToString(CREATE_CONSENT_REQ_JSON_PATH, UTF_8);
        CreateConsentReq expectedRequest = getCreateConsentsRequestTest();

        //When:
        CreateConsentReq actualRequest = jsonConverter.toObject(requestStringJson, CreateConsentReq.class).get();

        //Then:
        assertThat(actualRequest).isEqualTo(expectedRequest);
    }

    @Test
    public void shouldFail_createConsentReqValidation_json() throws IOException {
        //Given:
        String requestStringJson = IOUtils.resourceToString(CREATE_CONSENT_REQ_WRONG_JSON_PATH, UTF_8);

        CreateConsentReq actualRequest = jsonConverter.toObject(requestStringJson, CreateConsentReq.class).get();

        //When:
        Set<ConstraintViolation<CreateConsentReq>> actualViolations = validator.validate(actualRequest);

        //Then:
        assertThat(actualViolations.size()).isEqualTo(1);
    }

    @Test
    public void shouldFail_createConsentReqValidation_object() {
        //Given:
        CreateConsentReq wrongCreateConsentsRequest = getCreateConsentsRequestTest();
        wrongCreateConsentsRequest.setAccess(null);

        //When:
        Set<ConstraintViolation<CreateConsentReq>> actualOneViolation = validator.validate(wrongCreateConsentsRequest);

        //Then:
        assertThat(actualOneViolation.size()).isEqualTo(1);

        //Given:
        wrongCreateConsentsRequest.setValidUntil(null);

        //When:
        Set<ConstraintViolation<CreateConsentReq>> actualTwoViolations = validator.validate(wrongCreateConsentsRequest);

        //Then:
        assertThat(actualTwoViolations.size()).isEqualTo(2);
    }


    @Test
    public void createConsentReqValidation() throws IOException {
        //Given:
        String requestStringJson = IOUtils.resourceToString(CREATE_CONSENT_REQ_JSON_PATH, UTF_8);
        CreateConsentReq actualRequest = jsonConverter.toObject(requestStringJson, CreateConsentReq.class).get();

        //When:
        Set<ConstraintViolation<CreateConsentReq>> actualViolations = validator.validate(actualRequest);

        //Then:
        assertThat(actualViolations.size()).isEqualTo(0);
    }

    @Test
    public void createConsentNoDedicateAccountReq_jsonTest() throws IOException {
        //Given:
        String requestStringJson = IOUtils.resourceToString(NO_DEDICATE_REQ_PATH, UTF_8);
        CreateConsentReq expectedRequest = getAicNoDedicatedAccountRequest();

        //When:
        CreateConsentReq actualRequest = jsonConverter.toObject(requestStringJson, CreateConsentReq.class).get();

        //Then:
        assertThat(actualRequest).isEqualTo(expectedRequest);
    }

    private CreateConsentReq getAicNoDedicatedAccountRequest() {

        Xs2aAccountAccess accountAccess = new Xs2aAccountAccess(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), null, null);

        CreateConsentReq aicRequestObj = new CreateConsentReq();
        aicRequestObj.setAccess(accountAccess);
        aicRequestObj.setRecurringIndicator(true);
        aicRequestObj.setValidUntil(LocalDate.parse("2017-11-01"));
        aicRequestObj.setFrequencyPerDay(4);

        return aicRequestObj;
    }

    private CreateConsentReq getCreateConsentsRequestTest() {

        Xs2aAccountReference iban1 = new Xs2aAccountReference();
        iban1.setIban("DE2310010010123456789");

        Xs2aAccountReference iban2 = new Xs2aAccountReference();
        iban2.setIban("DE2310010010123456790");
        iban2.setCurrency(Currency.getInstance("USD"));

        Xs2aAccountReference iban3 = new Xs2aAccountReference();
        iban3.setIban("DE2310010010123456788");

        Xs2aAccountReference iban4 = new Xs2aAccountReference();
        iban4.setIban("DE2310010010123456789");

        Xs2aAccountReference maskedPan = new Xs2aAccountReference();
        maskedPan.setMaskedPan("123456xxxxxx1234");

        List<Xs2aAccountReference> balances = Arrays.asList(iban1, iban2, iban3);
        List<Xs2aAccountReference> transactions = Arrays.asList(iban4, maskedPan);

        Xs2aAccountAccess accountAccess = new Xs2aAccountAccess(Collections.emptyList(), balances, transactions, null, null);

        CreateConsentReq aicRequestObj = new CreateConsentReq();
        aicRequestObj.setAccess(accountAccess);
        aicRequestObj.setRecurringIndicator(true);
        aicRequestObj.setValidUntil(LocalDate.parse("2017-11-01"));
        aicRequestObj.setFrequencyPerDay(4);

        return aicRequestObj;
    }
}
