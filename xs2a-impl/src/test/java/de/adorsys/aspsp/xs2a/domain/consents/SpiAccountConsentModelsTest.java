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
import de.adorsys.aspsp.xs2a.domain.AccountReference;
import de.adorsys.aspsp.xs2a.domain.consent.AccountAccess;
import de.adorsys.aspsp.xs2a.domain.consent.AccountAccessType;
import de.adorsys.aspsp.xs2a.domain.consent.CreateConsentReq;
import de.adorsys.aspsp.xs2a.web.util.ApiDateConstants;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

public class SpiAccountConsentModelsTest {
    private static final String CREATE_CONSENT_REQ_JSON_PATH = "/json/CreateAccountConsentReqTest.json";
    private static final String ALL_ACCOUNTS_AVAILABLE_REQ_PATH = "/json/CreateConsentsAllAccountsAvailableReqTest.json";
    private static final String NO_DEDICATE_REQ_PATH = "/json/CreateConsentsNoDedicateAccountReqTest.json";
    private final String CREATE_CONSENT_REQ_WRONG_JSON_PATH = "/json/CreateAccountConsentReqWrongTest.json";
    private static final Charset UTF_8 = Charset.forName("utf-8");
    private ObjectMapper mapper = new ObjectMapper();
    private Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    public void createConsentReq_jsonTest() throws IOException {
        //Given:
        String requestStringJson = IOUtils.resourceToString(CREATE_CONSENT_REQ_JSON_PATH, UTF_8);
        CreateConsentReq expectedRequest = getCreateConsentsRequestTest();

        //When:
        CreateConsentReq actualRequest = mapper.readValue(requestStringJson, CreateConsentReq.class);

        //Then:
        assertThat(actualRequest).isEqualTo(expectedRequest);
    }

    @Test
    public void shouldFail_createConsentReqValidation_json() throws IOException {
        //Given:
        String requestStringJson = IOUtils.resourceToString(CREATE_CONSENT_REQ_WRONG_JSON_PATH, UTF_8);

        CreateConsentReq actualRequest = mapper.readValue(requestStringJson, CreateConsentReq.class);

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
        CreateConsentReq actualRequest = mapper.readValue(requestStringJson, CreateConsentReq.class);

        //When:
        Set<ConstraintViolation<CreateConsentReq>> actualViolations = validator.validate(actualRequest);

        //Then:
        assertThat(actualViolations.size()).isEqualTo(0);
    }

    @Test
    public void createConsentAllAccountsAvailableReq_jsonTest() throws IOException {
        //Given:
        String requestStringJson = IOUtils.resourceToString(ALL_ACCOUNTS_AVAILABLE_REQ_PATH, UTF_8);
        CreateConsentReq expectedRequest = getAicAvailableAccountsRequest();

        //When:
        CreateConsentReq actualRequest = mapper.readValue(requestStringJson, CreateConsentReq.class);

        //Then:
//        assertThat(actualRequest).isEqualTo(expectedRequest);
    }

    @Test
    public void createConsentNoDedicateAccountReq_jsonTest() throws IOException {
        //Given:
        String requestStringJson = IOUtils.resourceToString(NO_DEDICATE_REQ_PATH, UTF_8);
        CreateConsentReq expectedRequest = getAicNoDedicatedAccountRequest();

        //When:
        CreateConsentReq actualRequest = mapper.readValue(requestStringJson, CreateConsentReq.class);

        //Then:
        assertThat(actualRequest).isEqualTo(expectedRequest);
    }

    private CreateConsentReq getAicNoDedicatedAccountRequest() {

        AccountAccess accountAccess = new AccountAccess(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), null, null);

        CreateConsentReq aicRequestObj = new CreateConsentReq();
        aicRequestObj.setAccess(accountAccess);
        aicRequestObj.setRecurringIndicator(true);
        aicRequestObj.setValidUntil(getDateFromDateString("2017-11-01"));
        aicRequestObj.setFrequencyPerDay(4);

        return aicRequestObj;
    }

    private CreateConsentReq getAicAvailableAccountsRequest() {

        AccountAccess accountAccess = new AccountAccess(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), AccountAccessType.ALL_ACCOUNTS, null);

        CreateConsentReq aicRequestObj = new CreateConsentReq();
        aicRequestObj.setAccess(accountAccess);
        aicRequestObj.setRecurringIndicator(false);
        aicRequestObj.setValidUntil(getDateFromDateString("2017-08-06"));
        aicRequestObj.setFrequencyPerDay(1);

        return aicRequestObj;
    }

    private CreateConsentReq getCreateConsentsRequestTest() {

        AccountReference iban1 = new AccountReference();
        iban1.setIban("DE2310010010123456789");

        AccountReference iban2 = new AccountReference();
        iban2.setIban("DE2310010010123456790");
        iban2.setCurrency(Currency.getInstance("USD"));

        AccountReference iban3 = new AccountReference();
        iban3.setIban("DE2310010010123456788");

        AccountReference iban4 = new AccountReference();
        iban4.setIban("DE2310010010123456789");

        AccountReference maskedPan = new AccountReference();
        maskedPan.setMaskedPan("123456xxxxxx1234");

        List<AccountReference> balances = Arrays.asList(iban1, iban2, iban3);
        List<AccountReference> transactions =  Arrays.asList(iban4, maskedPan);

        AccountAccess accountAccess = new AccountAccess(null, balances, transactions, null, null);

        CreateConsentReq aicRequestObj = new CreateConsentReq();
        aicRequestObj.setAccess(accountAccess);
        aicRequestObj.setRecurringIndicator(true);
        aicRequestObj.setValidUntil(getDateFromDateString("2017-11-01"));
        aicRequestObj.setFrequencyPerDay(4);

        return aicRequestObj;
    }

    private static Date getDateFromDateString(String dateString) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat(ApiDateConstants.DATE_PATTERN);
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            return dateFormat.parse(dateString);
        } catch (ParseException e) {
            return null;
        }
    }
}
