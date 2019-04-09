package de.adorsys.psd2.xs2a.service.context;

import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.core.tpp.TppRole;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.TppService;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPsuDataMapper;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SpiContextDataProviderTest {
    private static final TppInfo TPP_INFO = buildTppInfo();
    private final static UUID X_REQUEST_ID = UUID.randomUUID();
    private static final PsuIdData PSU_DATA = new PsuIdData("psuId", "psuIdType", "psuCorporateId", "psuCorporateIdType");
    private static final SpiPsuData SPI_PSU_DATA = new SpiPsuData("psuId", "psuIdType", "psuCorporateId", "psuCorporateIdType");
    private static final SpiContextData SPI_CONTEXT_DATA = buildSpiContextData(null);
    private static final SpiContextData SPI_CONTEXT_DATA_WITH_PSU_DATA = buildSpiContextData(SPI_PSU_DATA);

    @InjectMocks
    private SpiContextDataProvider spiContextDataProvider;

    @Mock
    private TppService tppService;
    @Mock
    private Xs2aToSpiPsuDataMapper psuDataMapper;
    @Mock
    private RequestProviderService requestProviderService;

    @Test
    public void provide_success() {
        //Given
        when(tppService.getTppInfo())
            .thenReturn(TPP_INFO);
        when(requestProviderService.getRequestId())
            .thenReturn(X_REQUEST_ID);

        //When
        SpiContextData actualResponse = spiContextDataProvider.provide();

        //Then
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse).isEqualTo(SPI_CONTEXT_DATA);
    }

    @Test
    public void provideWithPsuIdData_success() {
        //Given
        when(tppService.getTppInfo())
            .thenReturn(TPP_INFO);
        when(requestProviderService.getRequestId())
            .thenReturn(X_REQUEST_ID);

        //When
        SpiContextData actualResponse = spiContextDataProvider.provideWithPsuIdData(PSU_DATA);

        //Then
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse).isEqualTo(SPI_CONTEXT_DATA);
    }

    @Test
    public void provide_withParameters_success() {
        //Given
        when(psuDataMapper.mapToSpiPsuData(PSU_DATA))
            .thenReturn(SPI_PSU_DATA);
        when(requestProviderService.getRequestId())
            .thenReturn(X_REQUEST_ID);

        //When
        SpiContextData actualResponse = spiContextDataProvider.provide(PSU_DATA, TPP_INFO);

        //Then
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse).isEqualTo(SPI_CONTEXT_DATA_WITH_PSU_DATA);
    }

    private static TppInfo buildTppInfo() {
        TppInfo tppInfo = new TppInfo();
        tppInfo.setAuthorisationNumber("registrationNumber");
        tppInfo.setTppName("tppName");
        tppInfo.setTppRoles(Collections.singletonList(TppRole.PISP));
        tppInfo.setAuthorityId("authorityId");
        return tppInfo;
    }

    private static SpiContextData buildSpiContextData(SpiPsuData spiPsuData) {
        return new SpiContextData(spiPsuData, TPP_INFO, X_REQUEST_ID);
    }
}
