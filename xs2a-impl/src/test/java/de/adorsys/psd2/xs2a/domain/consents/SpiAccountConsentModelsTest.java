/*
 * Copyright 2018-2022 adorsys GmbH & Co KG
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version. This program is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 *
 * This project is also available under a separate commercial license. You can
 * contact us at psd2@adorsys.com.
 */

package de.adorsys.psd2.xs2a.domain.consents;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.adorsys.psd2.core.data.AccountAccess;
import de.adorsys.psd2.mapper.Xs2aObjectMapper;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.domain.consent.CreateConsentReq;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class SpiAccountConsentModelsTest {
    private static final String CREATE_CONSENT_REQ_JSON_PATH = "/json/CreateAccountConsentReqTest.json";
    private static final String NO_DEDICATE_REQ_PATH = "/json/CreateConsentsNoDedicateAccountReqTest.json";
    private static final String CREATE_CONSENT_REQ_WRONG_JSON_PATH = "/json/CreateAccountConsentReqWrongTest.json";
    private static final Charset UTF_8 = StandardCharsets.UTF_8;

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    private final Xs2aObjectMapper xs2aObjectMapper = (Xs2aObjectMapper) new Xs2aObjectMapper().registerModule(new JavaTimeModule());

    @Test
    void createConsentReq_jsonTest() throws IOException {
        //Given:
        String requestStringJson = IOUtils.resourceToString(CREATE_CONSENT_REQ_JSON_PATH, UTF_8);
        CreateConsentReq expectedRequest = getCreateConsentsRequestTest();

        //When:
        CreateConsentReq actualRequest = xs2aObjectMapper.readValue(requestStringJson, CreateConsentReq.class);

        //Then:
        assertThat(actualRequest).isEqualTo(expectedRequest);
    }

    @Test
    void shouldFail_createConsentReqValidation_json() throws IOException {
        //Given:
        String requestStringJson = IOUtils.resourceToString(CREATE_CONSENT_REQ_WRONG_JSON_PATH, UTF_8);

        CreateConsentReq actualRequest = xs2aObjectMapper.readValue(requestStringJson, CreateConsentReq.class);

        //When:
        Set<ConstraintViolation<CreateConsentReq>> actualViolations = validator.validate(actualRequest);

        //Then:
        assertThat(actualViolations.size()).isEqualTo(1);
    }

    @Test
    void shouldFail_createConsentReqValidation_object() {
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
    void createConsentReqValidation() throws IOException {
        //Given:
        String requestStringJson = IOUtils.resourceToString(CREATE_CONSENT_REQ_JSON_PATH, UTF_8);
        CreateConsentReq actualRequest = xs2aObjectMapper.readValue(requestStringJson, CreateConsentReq.class);

        //When:
        Set<ConstraintViolation<CreateConsentReq>> actualViolations = validator.validate(actualRequest);

        //Then:
        assertThat(actualViolations).isEmpty();
    }

    @Test
    void createConsentNoDedicateAccountReq_jsonTest() throws IOException {
        //Given:
        String requestStringJson = IOUtils.resourceToString(NO_DEDICATE_REQ_PATH, UTF_8);
        CreateConsentReq expectedRequest = getAicNoDedicatedAccountRequest();

        //When:
        CreateConsentReq actualRequest = xs2aObjectMapper.readValue(requestStringJson, CreateConsentReq.class);

        //Then:
        assertThat(actualRequest).isEqualTo(expectedRequest);
    }

    private CreateConsentReq getAicNoDedicatedAccountRequest() {

        AccountAccess accountAccess = new AccountAccess(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), null);

        CreateConsentReq aicRequestObj = new CreateConsentReq();
        aicRequestObj.setAccess(accountAccess);
        aicRequestObj.setRecurringIndicator(true);
        aicRequestObj.setValidUntil(LocalDate.parse("2017-11-01"));
        aicRequestObj.setFrequencyPerDay(4);

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
        List<AccountReference> transactions = Arrays.asList(iban4, maskedPan);

        AccountAccess accountAccess = new AccountAccess(Collections.emptyList(), balances, transactions, null);

        CreateConsentReq aicRequestObj = new CreateConsentReq();
        aicRequestObj.setAccess(accountAccess);
        aicRequestObj.setRecurringIndicator(true);
        aicRequestObj.setValidUntil(LocalDate.parse("2017-11-01"));
        aicRequestObj.setFrequencyPerDay(4);

        return aicRequestObj;
    }
}
