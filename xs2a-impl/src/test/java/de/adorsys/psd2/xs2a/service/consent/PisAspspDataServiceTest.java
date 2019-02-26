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
        //given
        when(aspspDataService.readAspspConsentData(PAYMENT_ID))
            .thenReturn(Optional.of(ASPSP_CONSENT_DATA));

        //when
        AspspConsentData actualResponse = pisAspspDataService.getAspspConsentData(PAYMENT_ID);

        //then
        assertThat(actualResponse).isEqualTo(ASPSP_CONSENT_DATA);
    }

    @Test
    public void getAspspConsentData_with_nullAspspConsentData() {
        //given
        when(aspspDataService.readAspspConsentData(PAYMENT_ID))
            .thenReturn(Optional.empty());

        //when
        AspspConsentData actualResponse = pisAspspDataService.getAspspConsentData(PAYMENT_ID);

        //then
        assertThat(actualResponse).isEqualTo(ASPSP_CONSENT_DATA_NULL);
    }

    @Test
    public void getInternalPaymentIdByEncryptedString_success() {
        //given
        when(pisCommonPaymentServiceEncrypted.getDecryptedId(ENCRYPTED_ID))
            .thenReturn(Optional.of(PAYMENT_ID));

        //when
        String actualResponse = pisAspspDataService.getInternalPaymentIdByEncryptedString(ENCRYPTED_ID);

        //then
        assertThat(actualResponse).isEqualTo(PAYMENT_ID);
    }

    @Test
    public void getInternalPaymentIdByEncryptedString_failed() {
        //given
        when(pisCommonPaymentServiceEncrypted.getDecryptedId(ENCRYPTED_ID))
            .thenReturn(Optional.empty());

        //when
        String actualResponse = pisAspspDataService.getInternalPaymentIdByEncryptedString(ENCRYPTED_ID);

        //then
        assertThat(actualResponse).isNull();
    }
}
