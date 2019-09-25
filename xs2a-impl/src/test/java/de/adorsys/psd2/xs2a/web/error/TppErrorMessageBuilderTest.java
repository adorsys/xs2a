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

package de.adorsys.psd2.xs2a.web.error;

import de.adorsys.psd2.xs2a.service.message.MessageService;
import de.adorsys.psd2.xs2a.web.filter.TppErrorMessage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.CERTIFICATE_EXPIRED;
import static de.adorsys.psd2.xs2a.exception.MessageCategory.ERROR;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TppErrorMessageBuilderTest {
    private static final String TEXT = "Certificate is expired";
    private static final String CODE = "CERTIFICATE_EXPIRED";

    @InjectMocks
    private TppErrorMessageBuilder tppErrorMessageBuilder;
    @Mock
    private MessageService messageService;

    @Test
    public void buildTppErrorMessage() {
        TppErrorMessage expected = new TppErrorMessage(ERROR, CERTIFICATE_EXPIRED, TEXT);
        when(messageService.getMessage(CODE)).thenReturn(TEXT);

        TppErrorMessage actual = tppErrorMessageBuilder.buildTppErrorMessage(ERROR, CERTIFICATE_EXPIRED);

        assertEquals(expected, actual);
    }
}
