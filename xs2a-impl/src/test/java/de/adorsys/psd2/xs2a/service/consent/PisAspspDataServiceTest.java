package de.adorsys.psd2.xs2a.service.consent;

import de.adorsys.psd2.consent.api.service.PisCommonPaymentServiceEncrypted;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PisAspspDataServiceTest {
    private static final String PAYMENT_ID = "d6cb50e5-bb88-4bbf-a5c1-42ee1ed1df2c";
    private static final String ENCRYPTED_ID = "3278921mxl-n2131-13nw";
    private static final String WRONG_ID = "wrong id";

    @InjectMocks
    private PisAspspDataService pisAspspDataService;

    @Mock
    private PisCommonPaymentServiceEncrypted pisCommonPaymentServiceEncrypted;

    @Test
    public void getInternalPaymentIdByEncryptedString_success() {
        //Given
        when(pisCommonPaymentServiceEncrypted.getDecryptedId(ENCRYPTED_ID))
            .thenReturn(Optional.of(PAYMENT_ID));

        //When
        String actualResponse = pisAspspDataService.getInternalPaymentIdByEncryptedString(ENCRYPTED_ID);

        //Then
        assertThat(actualResponse).isEqualTo(PAYMENT_ID);
    }

    @Test
    public void getInternalPaymentIdByEncryptedString_failed() {
        //Given
        when(pisCommonPaymentServiceEncrypted.getDecryptedId(WRONG_ID))
            .thenReturn(Optional.empty());

        //When
        String actualResponse = pisAspspDataService.getInternalPaymentIdByEncryptedString(WRONG_ID);

        //Then
        assertThat(actualResponse).isNull();
    }
}
