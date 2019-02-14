package de.adorsys.psd2.xs2a.service.authorization.ais.stage.decoupled;

import de.adorsys.psd2.xs2a.config.factory.AisScaStageAuthorisationFactory;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.consent.UpdateConsentPsuDataReq;
import de.adorsys.psd2.xs2a.domain.consent.UpdateConsentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.authorization.ais.stage.AisScaStage;
import de.adorsys.psd2.xs2a.service.consent.AisConsentDataService;
import de.adorsys.psd2.xs2a.service.consent.Xs2aAisConsentService;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aAisConsentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiResponseStatusToXs2aMessageErrorCodeMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aAuthenticationObjectMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPsuDataMapper;
import de.adorsys.psd2.xs2a.spi.service.AisConsentSpi;
import org.springframework.stereotype.Service;

import static de.adorsys.psd2.xs2a.config.factory.AisScaStageAuthorisationFactory.SEPARATOR;
import static de.adorsys.psd2.xs2a.config.factory.AisScaStageAuthorisationFactory.SERVICE_PREFIX;

@Service("AIS_DECOUPLED_PSUIDENTIFIED")
public class AisDecoupledScaIdentifiedAuthorisationStage extends AisScaStage<UpdateConsentPsuDataReq, UpdateConsentPsuDataResponse> {
    private final AisScaStageAuthorisationFactory scaStageAuthorisationFactory;

    public AisDecoupledScaIdentifiedAuthorisationStage(Xs2aAisConsentService aisConsentService,
                                                       AisConsentDataService aisConsentDataService,
                                                       AisConsentSpi aisConsentSpi,
                                                       Xs2aAisConsentMapper aisConsentMapper,
                                                       SpiResponseStatusToXs2aMessageErrorCodeMapper messageErrorCodeMapper,
                                                       Xs2aToSpiPsuDataMapper psuDataMapper,
                                                       SpiToXs2aAuthenticationObjectMapper spiToXs2aAuthenticationObjectMapper,
                                                       SpiErrorMapper spiErrorMapper,
                                                       AisScaStageAuthorisationFactory scaStageAuthorisationFactory) {
        super(aisConsentService, aisConsentDataService, aisConsentSpi, aisConsentMapper, messageErrorCodeMapper, psuDataMapper, spiToXs2aAuthenticationObjectMapper, spiErrorMapper);
        this.scaStageAuthorisationFactory = scaStageAuthorisationFactory;
    }

    /**
     * Psu identified authorisation stage workflow: PSU authorising process using data from request
     * (returns response with FAILED status in case of non-successful authorising), available SCA methods getting
     * and performing the flow according to none, one or multiple available methods.
     *
     * @param request UpdateConsentPsuDataReq with updating data
     * @return UpdateConsentPsuDataResponse as a result of updating process
     */
    @Override
    public UpdateConsentPsuDataResponse apply(UpdateConsentPsuDataReq request) {
        AisScaStage<UpdateConsentPsuDataReq, UpdateConsentPsuDataResponse> service = scaStageAuthorisationFactory.getService(SERVICE_PREFIX + SEPARATOR + ScaApproach.DECOUPLED + SEPARATOR + ScaStatus.STARTED.name());
        return service.apply(request);
    }
}
