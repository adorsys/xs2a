package de.adorsys.psd2.xs2a.service.authorization.ais.stage.decoupled;

import de.adorsys.psd2.xs2a.config.factory.AisScaStageAuthorisationFactory;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.consent.AccountConsentAuthorization;
import de.adorsys.psd2.xs2a.domain.consent.UpdateConsentPsuDataReq;
import de.adorsys.psd2.xs2a.domain.consent.UpdateConsentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.authorization.ais.stage.AisScaStage;
import de.adorsys.psd2.xs2a.service.consent.Xs2aAisConsentService;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aAisConsentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aAuthenticationObjectMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPsuDataMapper;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.spi.service.AisConsentSpi;
import org.springframework.stereotype.Service;

import static de.adorsys.psd2.xs2a.config.factory.AisScaStageAuthorisationFactory.SEPARATOR;
import static de.adorsys.psd2.xs2a.config.factory.AisScaStageAuthorisationFactory.SERVICE_PREFIX;

@Service("AIS_DECOUPLED_PSUIDENTIFIED")
public class AisDecoupledScaIdentifiedAuthorisationStage extends AisScaStage<UpdateConsentPsuDataReq, AccountConsentAuthorization, UpdateConsentPsuDataResponse> {
    private final AisScaStageAuthorisationFactory scaStageAuthorisationFactory;

    public AisDecoupledScaIdentifiedAuthorisationStage(Xs2aAisConsentService aisConsentService,
                                                       SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory,
                                                       AisConsentSpi aisConsentSpi,
                                                       Xs2aAisConsentMapper aisConsentMapper,
                                                       Xs2aToSpiPsuDataMapper psuDataMapper,
                                                       SpiToXs2aAuthenticationObjectMapper spiToXs2aAuthenticationObjectMapper,
                                                       SpiErrorMapper spiErrorMapper,
                                                       AisScaStageAuthorisationFactory scaStageAuthorisationFactory) {
        super(aisConsentService, aspspConsentDataProviderFactory, aisConsentSpi, aisConsentMapper, psuDataMapper, spiToXs2aAuthenticationObjectMapper, spiErrorMapper);
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
    public UpdateConsentPsuDataResponse apply(UpdateConsentPsuDataReq request, AccountConsentAuthorization authorisationResponse) {
        AisScaStage<UpdateConsentPsuDataReq, AccountConsentAuthorization, UpdateConsentPsuDataResponse> service = scaStageAuthorisationFactory.getService(SERVICE_PREFIX + SEPARATOR + ScaApproach.DECOUPLED + SEPARATOR + ScaStatus.RECEIVED.name());
        return service.apply(request, authorisationResponse);
    }
}
