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

package de.adorsys.psd2.xs2a.web.validator.path;

import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.PARAMETER_NOT_SUPPORTED_WRONG_PAYMENT_TYPE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentProductAndTypeValidatorTest {

    private static final String CORRECT_PAYMENT_PRODUCT = "sepa-credit-transfers";
    private static final String WRONG_PAYMENT_PRODUCT = "sepa-credit-transfers111";
    private static final PaymentType UNSUPPORTED_PAYMENT_TYPE = PaymentType.PERIODIC;

    @InjectMocks
    private PaymentTypeAndProductValidator paymentProductAndTypeValidator;

    @Mock
    private AspspProfileServiceWrapper aspspProfileService;

    @BeforeEach
    void setUp() {
        Map<PaymentType, Set<String>> matrix = getSupportedPaymentTypeAndProductMatrix();
        when(aspspProfileService.getSupportedPaymentTypeAndProductMatrix()).thenReturn(matrix);
    }

    @Test
    void validatePaymentInitiationParams_correct() {
        //When:
        ValidationResult actual = paymentProductAndTypeValidator.validateTypeAndProduct(PaymentType.SINGLE, CORRECT_PAYMENT_PRODUCT);

        //Then:
        assertTrue(actual.isValid());
    }

    @Test
    void validatePaymentInitiationParams_wrongProduct() {
        //When:
        ValidationResult actual = paymentProductAndTypeValidator.validateTypeAndProduct(PaymentType.SINGLE, WRONG_PAYMENT_PRODUCT);

        //Then:
        assertTrue(actual.isNotValid());
        assertEquals(1, actual.getMessageError().getTppMessages().size());
        assertEquals(MessageErrorCode.PRODUCT_UNKNOWN_WRONG_PAYMENT_PRODUCT, actual.getMessageError().getTppMessage().getMessageErrorCode());
    }

    @Test
    void validatePaymentInitiationParams_unsupportedType() {
        //When:
        ValidationResult actual = paymentProductAndTypeValidator.validateTypeAndProduct(UNSUPPORTED_PAYMENT_TYPE, CORRECT_PAYMENT_PRODUCT);

        //Then:
        assertTrue(actual.isNotValid());
        assertEquals(1, actual.getMessageError().getTppMessages().size());
        assertEquals(PARAMETER_NOT_SUPPORTED_WRONG_PAYMENT_TYPE, actual.getMessageError().getTppMessage().getMessageErrorCode());
    }

    private Map<PaymentType, Set<String>> getSupportedPaymentTypeAndProductMatrix() {
        Map<PaymentType, Set<String>> matrix = new HashMap<>();
        Set<String> availablePaymentProducts = new HashSet<>(Collections.singletonList(CORRECT_PAYMENT_PRODUCT));
        matrix.put(PaymentType.SINGLE, availablePaymentProducts);
        matrix.put(PaymentType.BULK, availablePaymentProducts);
        return matrix;
    }
}
