package de.adorsys.psd2.xs2a.service.consent;

import de.adorsys.psd2.consent.api.service.PisCommonPaymentServiceEncrypted;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PisPsuDataServiceTest {
    private static final String PAYMENT_ID = "d6cb50e5-bb88-4bbf-a5c1-42ee1ed1df2c";
    private static final String WRONG_ID = "wrong id";
    private static final List<PsuIdData> LIST_PSU_DATA = getListPisPayment();

    @InjectMocks
    private PisPsuDataService pisPsuDataService;

    @Mock
    private PisCommonPaymentServiceEncrypted pisCommonPaymentServiceEncrypted;

    @Test
    public void getPsuDataByPaymentId_success() {
        //Given
        when(pisCommonPaymentServiceEncrypted.getPsuDataListByPaymentId(PAYMENT_ID))
            .thenReturn(Optional.of(LIST_PSU_DATA));

        //When
        List<PsuIdData> actualResponse = pisPsuDataService.getPsuDataByPaymentId(PAYMENT_ID);

        //Then
        assertThat(actualResponse).isEqualTo(LIST_PSU_DATA);
    }

    @Test
    public void getPsuDataByPaymentId_failed() {
        //Given
        when(pisCommonPaymentServiceEncrypted.getPsuDataListByPaymentId(WRONG_ID))
            .thenReturn(Optional.empty());

        //When
        List<PsuIdData> actualResponse = pisPsuDataService.getPsuDataByPaymentId(WRONG_ID);

        //Then
        assertThat(actualResponse).isNull();
    }

    private static List<PsuIdData> getListPisPayment() {
        return Collections.singletonList(new PsuIdData("psuId", "psuIdType", "psuCorporateId", "psuCorporateIdType"));
    }
}
