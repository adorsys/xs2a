package de.adorsys.psd2.xs2a.service.consent;

import de.adorsys.psd2.consent.api.AspspDataService;
import de.adorsys.psd2.consent.api.service.PisCommonPaymentServiceEncrypted;
import de.adorsys.psd2.xs2a.core.consent.AspspConsentData;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PisAspspDataServiceTest {
    private static final String PAYMENT_ID = "d6cb50e5-bb88-4bbf-a5c1-42ee1ed1df2c";
    private static final String ENCRYPTED_ID = "3278921mxl-n2131-13nw";
    private static final String WRONG_ID = "wrong id";
    private static final AspspConsentData ASPSP_CONSENT_DATA = new AspspConsentData(new byte[16], PAYMENT_ID);
    private static final AspspConsentData ASPSP_CONSENT_DATA_NULL = new AspspConsentData(null, PAYMENT_ID);

    @InjectMocks
    private PisAspspDataService pisAspspDataService;

    @Mock
    private PisCommonPaymentServiceEncrypted pisCommonPaymentServiceEncrypted;
    @Mock
    private AspspDataService aspspDataService;

    @Test
    public void getAspspConsentData_success() {
        //Given
        when(aspspDataService.readAspspConsentData(PAYMENT_ID))
            .thenReturn(Optional.of(ASPSP_CONSENT_DATA));

        //When
        AspspConsentData actualResponse = pisAspspDataService.getAspspConsentData(PAYMENT_ID);

        //Then
        assertThat(actualResponse).isEqualTo(ASPSP_CONSENT_DATA);
    }

    @Test
    public void getAspspConsentData_emptyAspspConsentData_success() {
        //Given
        when(aspspDataService.readAspspConsentData(PAYMENT_ID))
            .thenReturn(Optional.empty());

        //When
        AspspConsentData actualResponse = pisAspspDataService.getAspspConsentData(PAYMENT_ID);

        //Then
        assertThat(actualResponse).isEqualTo(ASPSP_CONSENT_DATA_NULL);
    }

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
