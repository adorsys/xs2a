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

package de.adorsys.psd2.xs2a.service.validator;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.validation.Validation;
import javax.validation.ValidationException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class ValueValidatorServiceTest {
    private static final String ACCOUNT_ID = "11111111";
    private static final String TRANSACTION_ID = "22222222";

    @InjectMocks
    private ValueValidatorService valueValidatorService;

    @BeforeEach
    void setUp() {
        valueValidatorService = new ValueValidatorService(Validation.buildDefaultValidatorFactory().getValidator());
    }

    @Test
    void validateAccountIdTransactionId() {
        //When Then:
        valueValidatorService.validateAccountIdTransactionId(ACCOUNT_ID, TRANSACTION_ID);
    }

    @Test
    void shouldFail_validateAccountIdTransactionId_accountIdNull() {
        //When Then:
        assertThatThrownBy(() -> valueValidatorService.validateAccountIdTransactionId(null, TRANSACTION_ID))
            .isInstanceOf(ValidationException.class);
    }

    @Test
    void shouldFail_validateAccountIdTransactionId_transactionIdNull() {
        //When Then:
        assertThatThrownBy(() -> valueValidatorService.validateAccountIdTransactionId(ACCOUNT_ID, null))
            .isInstanceOf(ValidationException.class);
    }

    @Test
    void validate_AccountAndTransaction() {
        //Given:
        ValidationGroup fields = new ValidationGroup();
        fields.setAccountId(ACCOUNT_ID);
        fields.setTransactionId(TRANSACTION_ID);

        //When Then:
        valueValidatorService.validate(fields, ValidationGroup.AccountIdGroup.class, ValidationGroup.TransactionIdGroup.class);
    }

    @Test
    void shouldFail_validate_AccountAndEmptyTransaction() {
        //Given:
        ValidationGroup fields = new ValidationGroup();
        fields.setAccountId(ACCOUNT_ID);

        //When Then:
        assertThatThrownBy(() -> valueValidatorService.validate(fields, ValidationGroup.AccountIdGroup.class, ValidationGroup.TransactionIdGroup.class))
            .isInstanceOf(ValidationException.class);
    }

    @Test
    void shouldFail_validate_EmptyAccountAndTransaction() {
        //Given:
        ValidationGroup fields = new ValidationGroup();
        fields.setTransactionId(TRANSACTION_ID);

        //When Then:
        assertThatThrownBy(() -> valueValidatorService.validate(fields, ValidationGroup.AccountIdGroup.class, ValidationGroup.TransactionIdGroup.class))
            .isInstanceOf(ValidationException.class);
    }
}
