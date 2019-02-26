package de.adorsys.psd2.xs2a.service.context;

import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.core.tpp.TppRole;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SpiContextDataProviderTest {
    private static final TppInfo TPP_INFO = buildTppInfo();
    private static final PsuIdData PSU_DATA = new PsuIdData("psuId", "psuIdType", "psuCorporateId", "psuCorporateIdType");
    private static final SpiPsuData SPI_PSU_DATA = new SpiPsuData("psuId", "psuIdType", "psuCorporateId", "psuCorporateIdType");
    private static final SpiContextData SPI_CONTEXT_DATA = getSpiContextData(null);
    private static final SpiContextData SPI_CONTEXT_DATA_WITH_SPI_PSU_DATA = getSpiContextData(SPI_PSU_DATA);

    @InjectMocks
    private SpiContextDataProvider spiContextDataProvider;

    @Mock
    private TppService tppService;
    @Mock
    private Xs2aToSpiPsuDataMapper psuDataMapper;

    @Test
    public void provideWithPsuIdData() {
        //given
        when(tppService.getTppInfo())
            .thenReturn(TPP_INFO);

        //when
        SpiContextData actualResponse = spiContextDataProvider.provideWithPsuIdData(PSU_DATA);

        //then
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse).isEqualTo(SPI_CONTEXT_DATA);
    }

    @Test
    public void provide() {
        //given
        when(psuDataMapper.mapToSpiPsuData(PSU_DATA))
            .thenReturn(SPI_PSU_DATA);

        //when
        SpiContextData actualResponse = spiContextDataProvider.provide(PSU_DATA, TPP_INFO);

        //then
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse).isEqualTo(SPI_CONTEXT_DATA_WITH_SPI_PSU_DATA);
    }

    private static TppInfo buildTppInfo() {
        TppInfo tppInfo = new TppInfo();
        tppInfo.setAuthorisationNumber("registrationNumber");
        tppInfo.setTppName("tppName");
        tppInfo.setTppRoles(Collections.singletonList(TppRole.PISP));
        tppInfo.setAuthorityId("authorityId");
        return tppInfo;
    }

    private static SpiContextData getSpiContextData(SpiPsuData spiPsuData) {
        SpiContextData spiContextData = new SpiContextData(spiPsuData, TPP_INFO);
        return spiContextData;
    }
}
