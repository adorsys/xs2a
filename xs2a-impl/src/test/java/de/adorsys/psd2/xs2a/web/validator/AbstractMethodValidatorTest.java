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
import de.adorsys.psd2.xs2a.web.validator.header.HeaderValidator;
import de.adorsys.psd2.xs2a.web.validator.path.PathParameterValidator;
import de.adorsys.psd2.xs2a.web.validator.query.QueryParameterValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AbstractMethodValidatorTest {
    private static final String QUERY_PARAMETER_NAME = "some-query-param";
    private static final String QUERY_PARAMETER_VALUE = "some value";

    @Mock
    private HeaderValidator headerValidator;
    @Mock
    private BodyValidator bodyValidator;
    @Mock
    private QueryParameterValidator queryParameterValidator;
    @Mock
    private PathParameterValidator pathParameterValidator;

    @Captor
    private ArgumentCaptor<Map<String, String>> headersCaptor;
    @Captor
    private ArgumentCaptor<Map<String, List<String>>> queryParametersCaptor;


    private MethodValidator methodValidator;
    private MessageError messageError;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        messageError = new MessageError();
        request = new MockHttpServletRequest();
        request.addHeader("Content-Type", "application/json");
        request.addParameter(QUERY_PARAMETER_NAME, QUERY_PARAMETER_VALUE);

        methodValidator = new AbstractMethodValidator(ValidatorWrapper.builder()
                                                          .headerValidators(Collections.singletonList(headerValidator))
                                                          .bodyValidators(Collections.singletonList(bodyValidator))
                                                          .queryParameterValidators(Collections.singletonList(queryParameterValidator))
                                                          .pathParameterValidators(Collections.singletonList(pathParameterValidator))
                                                          .build()) {
            @Override
            public String getMethodName() {
                return "method_name";
            }
        };
    }

    @Test
    void validate() {
        messageError = methodValidator.validate(request, messageError);

        verify(headerValidator, times(1)).validate(headersCaptor.capture(), eq(messageError));
        verify(bodyValidator, times(1)).validate(request, messageError);
        verify(queryParameterValidator, times(1)).validate(queryParametersCaptor.capture(), eq(messageError));

        assertEquals(1, headersCaptor.getValue().size());
        assertEquals("application/json", headersCaptor.getValue().get("Content-Type"));
        assertEquals(Collections.singletonList(QUERY_PARAMETER_VALUE), queryParametersCaptor.getValue().get(QUERY_PARAMETER_NAME));
    }
}
