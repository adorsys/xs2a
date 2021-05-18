/*
 * Copyright 2018-2021 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.service.validator;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ValidationGroupTest {
    private static final String ACCOUNT_ID = "11111111";
    private static final String TRANSACTION_ID = "22222222";

    @Test
    void creatingValidationGroupObject() {
        //Given
        ValidationGroup actual = new ValidationGroup();
        actual.setAccountId(ACCOUNT_ID);
        actual.setTransactionId(TRANSACTION_ID);

        //Then
        assertThat(actual.getAccountId())
            .isNotEmpty()
            .isEqualTo(ACCOUNT_ID);
        assertThat(actual.getTransactionId())
            .isNotEmpty()
            .isEqualTo(TRANSACTION_ID);
    }
}
