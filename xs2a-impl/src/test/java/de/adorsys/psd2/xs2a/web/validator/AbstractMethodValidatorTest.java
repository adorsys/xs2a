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
import de.adorsys.psd2.xs2a.web.validator.header.HeaderValidator;
import de.adorsys.psd2.xs2a.web.validator.path.PathParameterValidator;
import de.adorsys.psd2.xs2a.web.validator.query.QueryParameterValidator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AbstractMethodValidatorTest {
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

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() {
        messageError = new MessageError();
        request = new MockHttpServletRequest();
        request.addHeader("Content-Type", "application/json");
        request.addParameter(QUERY_PARAMETER_NAME, QUERY_PARAMETER_VALUE);

        methodValidator = new AbstractMethodValidator(Collections.singletonList(headerValidator),
                                                      Collections.singletonList(bodyValidator),
                                                      Collections.singletonList(queryParameterValidator),
                                                      Collections.singletonList(pathParameterValidator)) {
            @Override
            public String getMethodName() {
                return "method_name";
            }
        };
    }

    @Test
    public void validate() {
        methodValidator.validate(request, messageError);

        verify(headerValidator, times(1)).validate(headersCaptor.capture(), eq(messageError));
        verify(bodyValidator, times(1)).validate(eq(request), eq(messageError));
        verify(queryParameterValidator, times(1)).validate(queryParametersCaptor.capture(), eq(messageError));

        assertEquals(1, headersCaptor.getValue().size());
        assertEquals("application/json", headersCaptor.getValue().get("Content-Type"));
        assertEquals(Collections.singletonList(QUERY_PARAMETER_VALUE), queryParametersCaptor.getValue().get(QUERY_PARAMETER_NAME));
    }
}
