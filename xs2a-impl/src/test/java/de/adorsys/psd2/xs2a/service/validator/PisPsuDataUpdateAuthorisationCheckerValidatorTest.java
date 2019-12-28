package de.adorsys.psd2.xs2a.service.validator;

import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.UUID;

import static de.adorsys.psd2.xs2a.core.domain.TppMessageInformation.of;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.FORMAT_ERROR_NO_PSU;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.PSU_CREDENTIALS_INVALID;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PisPsuDataUpdateAuthorisationCheckerValidatorTest {
    @Mock
    private PsuDataUpdateAuthorisationChecker psuDataUpdateAuthorisationChecker;
    @Mock
    private RequestProviderService requestProviderService;
    @InjectMocks
    private PisPsuDataUpdateAuthorisationCheckerValidator pisPsuDataUpdateAuthorisationCheckerValidator;

    private static final PsuIdData EMPTY_PSU = new PsuIdData(null, null, null, null, null);
    private static final PsuIdData PSU_ID_DATA_1 = new PsuIdData("psu-id", null, null, null, null);
    private static final PsuIdData PSU_ID_DATA_2 = new PsuIdData("psu-id-2", null, null, null, null);

    private static final MessageError FORMAT_BOTH_PSUS_ABSENT_ERROR = new MessageError(ErrorType.PIS_400, of(FORMAT_ERROR_NO_PSU));
    private static final MessageError CREDENTIALS_INVALID_ERROR = new MessageError(ErrorType.PIS_401, of(PSU_CREDENTIALS_INVALID));

    @Before
    public void setUp() {
        when(requestProviderService.getInternalRequestId()).thenReturn(UUID.randomUUID());
        when(requestProviderService.getRequestId()).thenReturn(UUID.randomUUID());
    }

    @Test
    public void validate_withBothPsusAbsent_shouldReturnFormatError() {
        //Given
        when(psuDataUpdateAuthorisationChecker.areBothPsusAbsent(EMPTY_PSU, null))
            .thenReturn(true);

        //When
        ValidationResult validationResult = pisPsuDataUpdateAuthorisationCheckerValidator.validate(EMPTY_PSU, null);

        //Then
        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(FORMAT_BOTH_PSUS_ABSENT_ERROR, validationResult.getMessageError());
    }

    @Test
    public void validate_cantPsuUpdateAuthorisation_shouldReturnCredentialsInvalidError() {
        //When
        ValidationResult validationResult = pisPsuDataUpdateAuthorisationCheckerValidator.validate(PSU_ID_DATA_1, PSU_ID_DATA_2);

        //Then
        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(CREDENTIALS_INVALID_ERROR, validationResult.getMessageError());
    }

    @Test
    public void validate_successful() {
        when(psuDataUpdateAuthorisationChecker.canPsuUpdateAuthorisation(PSU_ID_DATA_1, PSU_ID_DATA_1))
            .thenReturn(true);

        ValidationResult validationResult = pisPsuDataUpdateAuthorisationCheckerValidator.validate(PSU_ID_DATA_1, PSU_ID_DATA_1);

        assertNotNull(validationResult);
        assertTrue(validationResult.isValid());
        assertNull(validationResult.getMessageError());
    }
}
