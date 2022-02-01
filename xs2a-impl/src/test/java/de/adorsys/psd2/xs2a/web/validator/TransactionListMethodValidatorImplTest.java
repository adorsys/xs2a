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

package de.adorsys.psd2.xs2a.web.validator;

import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.web.validator.body.BodyValidator;
import de.adorsys.psd2.xs2a.web.validator.body.consent.TransactionListBodyValidator;
import de.adorsys.psd2.xs2a.web.validator.header.HeaderValidator;
import de.adorsys.psd2.xs2a.web.validator.header.account.TransactionListHeaderValidator;
import de.adorsys.psd2.xs2a.web.validator.query.QueryParameterValidator;
import de.adorsys.psd2.xs2a.web.validator.query.account.TransactionListQueryParamsValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TransactionListMethodValidatorImplTest {
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

    @BeforeEach
    void setUp() {
        transactionListMethodValidator = new TransactionListMethodValidatorImpl(Collections.singletonList(transactionListHeaderValidator),
                                                                                Collections.singletonList(transactionListBodyValidator),
                                                                                Collections.singletonList(transactionListQueryParamsValidator));
    }

    @Test
    void validate_shouldPassQueryParametersToValidators() {
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
    void validate_withNoQueryParamsInRequest_shouldPassEmptyMap() {
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
    void validate_withMultipleValuesForOneParam_shouldPassValuesInList() {
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
    void getValidators_shouldReturnValidatorsFromConstructors() {
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
    void getMethodName_shouldReturnCorrectName() {
        // When
        String actualName = transactionListMethodValidator.getMethodName();

        // Then
        assertEquals(METHOD_NAME, actualName);
    }
}
