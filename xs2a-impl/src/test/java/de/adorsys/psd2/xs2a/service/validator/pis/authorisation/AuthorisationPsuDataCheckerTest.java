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

package de.adorsys.psd2.xs2a.service.validator.pis.authorisation;

import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.service.validator.authorisation.AuthorisationPsuDataChecker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class AuthorisationPsuDataCheckerTest {

    private static final PsuIdData PSU_ID_DATA = new PsuIdData("PSU ID", null, null, null, null);
    private static final PsuIdData PSU_ID_DATA_2 = new PsuIdData("PSU ID 2", null, null, null, null);

    @InjectMocks
    private AuthorisationPsuDataChecker authorisationPsuDataChecker;


    @Test
    void isPsuDataWrong_withoutMultilevelSca_samePsuId_shouldReturnFalse() {
        // When
        boolean result = authorisationPsuDataChecker.isPsuDataWrong(false, Collections.singletonList(PSU_ID_DATA), PSU_ID_DATA);

        // Then
        assertFalse(result);
    }

    @Test
    void isPsuDataWrong_withoutMultilevelSca_differentPsuId_shouldReturnTrue() {
        // When
        boolean result = authorisationPsuDataChecker.isPsuDataWrong(false, Collections.singletonList(PSU_ID_DATA), PSU_ID_DATA_2);

        // Then
        assertTrue(result);
    }

    @Test
    void isPsuDataWrong_withMultilevelSca_samePsuId_shouldReturnFalse() {
        // When
        boolean result = authorisationPsuDataChecker.isPsuDataWrong(true, Collections.singletonList(PSU_ID_DATA), PSU_ID_DATA);

        // Then
        assertFalse(result);
    }

    @Test
    void isPsuDataWrong_withMultilevelSca_differentPsuId_shouldReturnFalse() {
        // When
        boolean result = authorisationPsuDataChecker.isPsuDataWrong(true, Collections.singletonList(PSU_ID_DATA), PSU_ID_DATA_2);

        // Then
        assertFalse(result);
    }

}
