/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.web.validator;

import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.web.validator.body.BodyValidator;
import de.adorsys.psd2.xs2a.web.validator.body.consent.TransactionListBodyValidator;
import de.adorsys.psd2.xs2a.web.validator.header.HeaderValidator;
import de.adorsys.psd2.xs2a.web.validator.header.account.TransactionListHeaderValidator;
import de.adorsys.psd2.xs2a.web.validator.query.QueryParameterValidator;
import de.adorsys.psd2.xs2a.web.validator.query.account.TransactionListQueryParamsValidator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class TransactionListMethodValidatorImplTest {
    private static final String METHOD_NAME = "_getTransactionList";
    private static final String QUERY_PARAMETER_NAME = "some parameter name";
    private static final String QUERY_PARAMETER_VALUE = "some parameter value";
    private static final String ANOTHER_QUERY_PARAMETER_VALUE = "some another value";

    private TransactionListMethodValidatorImpl transactionListMethodValidator;
    @Mock
    private TransactionListHeaderValidator transactionListHeaderValidator;
    @Mock
    private TransactionListBodyValidator transactionListBodyValidator;
    @Mock
    private TransactionListQueryParamsValidator transactionListQueryParamsValidator;
    @Mock
    private MessageError messageError;
    private MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();

    @Before
    public void setUp() {
        transactionListMethodValidator = new TransactionListMethodValidatorImpl(Collections.singletonList(transactionListHeaderValidator),
                                                                                Collections.singletonList(transactionListBodyValidator),
                                                                                Collections.singletonList(transactionListQueryParamsValidator));
    }

    @Test
    public void validate_shouldPassQueryParametersToValidators() {
        // Given
        mockHttpServletRequest.addParameter(QUERY_PARAMETER_NAME, QUERY_PARAMETER_VALUE);

        Map<String, List<String>> expectedParams = new HashMap<>();
        expectedParams.put(QUERY_PARAMETER_NAME, Collections.singletonList(QUERY_PARAMETER_VALUE));

        // noinspection unchecked
        ArgumentCaptor<Map<String, List<String>>> queryParamCaptor = ArgumentCaptor.forClass(Map.class);

        // When
        transactionListMethodValidator.validate(mockHttpServletRequest, messageError);

        // Then
        verify(transactionListQueryParamsValidator).validate(queryParamCaptor.capture(), eq(messageError));
        assertEquals(expectedParams, queryParamCaptor.getValue());
    }

    @Test
    public void validate_withNoQueryParamsInRequest_shouldPassEmptyMap() {
        // Given
        // noinspection unchecked
        ArgumentCaptor<Map<String, List<String>>> queryParamCaptor = ArgumentCaptor.forClass(Map.class);

        // When
        transactionListMethodValidator.validate(mockHttpServletRequest, messageError);

        // Then
        verify(transactionListQueryParamsValidator).validate(queryParamCaptor.capture(), eq(messageError));
        assertTrue(queryParamCaptor.getValue().isEmpty());
    }

    @Test
    public void validate_withMultipleValuesForOneParam_shouldPassValuesInList() {
        // Given
        mockHttpServletRequest.addParameter(QUERY_PARAMETER_NAME, QUERY_PARAMETER_VALUE);
        mockHttpServletRequest.addParameter(QUERY_PARAMETER_NAME, ANOTHER_QUERY_PARAMETER_VALUE);

        Map<String, List<String>> expectedParams = new HashMap<>();
        expectedParams.put(QUERY_PARAMETER_NAME, Arrays.asList(QUERY_PARAMETER_VALUE, ANOTHER_QUERY_PARAMETER_VALUE));

        // noinspection unchecked
        ArgumentCaptor<Map<String, List<String>>> queryParamCaptor = ArgumentCaptor.forClass(Map.class);


        // When
        transactionListMethodValidator.validate(mockHttpServletRequest, messageError);

        // Then
        verify(transactionListQueryParamsValidator).validate(queryParamCaptor.capture(), eq(messageError));
        assertEquals(expectedParams, queryParamCaptor.getValue());
    }

    @Test
    public void getValidators_shouldReturnValidatorsFromConstructors() {
        // When
        List<? extends QueryParameterValidator> actualQueryValidators = transactionListMethodValidator.getValidatorWrapper().getQueryParameterValidators();
        List<? extends HeaderValidator> actualHeaderValidators = transactionListMethodValidator.getValidatorWrapper().getHeaderValidators();
        List<? extends BodyValidator> actualBodyValidators = transactionListMethodValidator.getValidatorWrapper().getBodyValidators();

        // Then
        assertEquals(Collections.singletonList(transactionListHeaderValidator), actualHeaderValidators);
        assertEquals(Collections.singletonList(transactionListBodyValidator), actualBodyValidators);
        assertEquals(Collections.singletonList(transactionListQueryParamsValidator), actualQueryValidators);
    }

    @Test
    public void getMethodName_shouldReturnCorrectName() {
        // When
        String actualName = transactionListMethodValidator.getMethodName();

        // Then
        assertEquals(METHOD_NAME, actualName);
    }
}
