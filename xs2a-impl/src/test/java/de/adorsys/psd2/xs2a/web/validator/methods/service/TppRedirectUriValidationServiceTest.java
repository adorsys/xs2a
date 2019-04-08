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

package de.adorsys.psd2.xs2a.web.validator.methods.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class TppRedirectUriValidationServiceTest {
    private static final boolean TPP_REDIRECT_PREFERRED_TRUE = true;
    private static final boolean TPP_REDIRECT_PREFERRED_FALSE = false;
    private static final String TPP_REDIRECT_URI_EXISTING = "TPP redirect URI";
    private static final String TPP_REDIRECT_URI_MISSING = null;

    @InjectMocks
    private TppRedirectUriValidationService tppRedirectUriValidationService;

    @Test
    public void isNotValid_True_TppRedirectPreferredIsTrueAndUriMissing() {
        boolean notValid = tppRedirectUriValidationService.isNotValid(TPP_REDIRECT_PREFERRED_TRUE, TPP_REDIRECT_URI_MISSING);

        assertThat(notValid).isTrue();
    }

    @Test
    public void isNotValid_False_TppRedirectPreferredIsTrueAndUriExisting() {
        boolean notValid = tppRedirectUriValidationService.isNotValid(TPP_REDIRECT_PREFERRED_TRUE, TPP_REDIRECT_URI_EXISTING);

        assertThat(notValid).isFalse();
    }

    @Test
    public void isNotValid_False_TppRedirectPreferredIsFalseAndUriExisting() {
        boolean notValid = tppRedirectUriValidationService.isNotValid(TPP_REDIRECT_PREFERRED_FALSE, TPP_REDIRECT_URI_EXISTING);

        assertThat(notValid).isFalse();
    }

    @Test
    public void isNotValid_False_TppRedirectPreferredIsFalseAndUriMissing() {
        boolean notValid = tppRedirectUriValidationService.isNotValid(TPP_REDIRECT_PREFERRED_FALSE, TPP_REDIRECT_URI_MISSING);

        assertThat(notValid).isFalse();
    }
}
