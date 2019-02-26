package de.adorsys.psd2.xs2a.service.consent;

import de.adorsys.psd2.consent.api.AspspDataService;
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
public class AisConsentDataServiceTest {
    private static final AspspConsentData ASPSP_CONSENT_DATA = new AspspConsentData(new byte[16], "some consent id");
    private static final AspspConsentData ASPSP_CONSENT_DATA_NULL = new AspspConsentData(null, "some consent id");

    @InjectMocks
    private AisConsentDataService aisConsentDataService;
    @Mock
    private AspspDataService aspspDataService;

    @Test
    public void getAspspConsentDataByConsentId_success() {
        //given
        when(aspspDataService.readAspspConsentData("some consent id")).thenReturn(Optional.of(ASPSP_CONSENT_DATA));

        //when
        AspspConsentData actualResponse = aisConsentDataService.getAspspConsentDataByConsentId("some consent id");

        //then
        assertThat(actualResponse).isEqualTo(ASPSP_CONSENT_DATA);
    }

    @Test
    public void getAspspConsentDataByConsentId_with_nullAspspConsentData() {
        //given
        when(aspspDataService.readAspspConsentData("some consent id")).thenReturn(Optional.empty());

        //when
        AspspConsentData actualResponse = aisConsentDataService.getAspspConsentDataByConsentId("some consent id");

        //then
        assertThat(actualResponse).isEqualTo(ASPSP_CONSENT_DATA_NULL);
    }
}
