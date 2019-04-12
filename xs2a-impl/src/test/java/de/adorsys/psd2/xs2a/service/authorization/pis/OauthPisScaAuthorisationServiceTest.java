package de.adorsys.psd2.xs2a.service.authorization.pis;

import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aAuthorisationSubResources;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreatePisAuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreatePisCancellationAuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aPaymentCancellationAuthorisationSubResource;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataRequest;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class OauthPisScaAuthorisationServiceTest {
    private static final String PAYMENT_ID = "c713a32c-15ff-4f90-afa0-34a500359844";
    private static final String AUTHORISATION_ID = "ad746cb3-a01b-4196-a6b9-40b0e4cd2350";
    private static final String CANCELLATION_AUTHORISATION_ID = "dd5d766f-eeb7-4efe-b730-24d5ed53f537";
    private static final PaymentType PAYMENT_TYPE = PaymentType.SINGLE;
    private static final PsuIdData PSU_ID_DATA = new PsuIdData("Test psuId", null, null, null);
    private static final Xs2aUpdatePisCommonPaymentPsuDataRequest XS2A_UPDATE_PIS_COMMON_PAYMENT_PSU_DATA_REQUEST = new Xs2aUpdatePisCommonPaymentPsuDataRequest();

    @InjectMocks
    private OauthPisScaAuthorisationService oauthPisScaAuthorisationService;

    @Test
    public void createCommonPaymentAuthorisation_success() {
        // When
        Optional<Xs2aCreatePisAuthorisationResponse> actualResponse = oauthPisScaAuthorisationService.createCommonPaymentAuthorisation(PAYMENT_ID, PAYMENT_TYPE, PSU_ID_DATA);

        // Then
        assertThat(actualResponse.isPresent()).isFalse();
    }

    @Test
    public void updateCommonPaymentPsuData_success() {
        // When
        Xs2aUpdatePisCommonPaymentPsuDataResponse actualResponse = oauthPisScaAuthorisationService.updateCommonPaymentPsuData(XS2A_UPDATE_PIS_COMMON_PAYMENT_PSU_DATA_REQUEST);

        // Then
        assertThat(actualResponse).isNull();
    }

    @Test
    public void createCommonPaymentCancellationAuthorisation_success() {
        // When
        Optional<Xs2aCreatePisCancellationAuthorisationResponse> actualResponse = oauthPisScaAuthorisationService.createCommonPaymentCancellationAuthorisation(PAYMENT_ID, PAYMENT_TYPE, PSU_ID_DATA);

        // Then
        assertThat(actualResponse.isPresent()).isFalse();
    }

    @Test
    public void getCancellationAuthorisationSubResources_success() {
        // When
        Optional<Xs2aPaymentCancellationAuthorisationSubResource> actualResponse = oauthPisScaAuthorisationService.getCancellationAuthorisationSubResources(PAYMENT_ID);

        // Then
        assertThat(actualResponse.isPresent()).isFalse();
    }

    @Test
    public void updateCommonPaymentCancellationPsuData_success() {
        // When
        Xs2aUpdatePisCommonPaymentPsuDataResponse actualResponse = oauthPisScaAuthorisationService.updateCommonPaymentCancellationPsuData(XS2A_UPDATE_PIS_COMMON_PAYMENT_PSU_DATA_REQUEST);

        // Then
        assertThat(actualResponse).isNull();
    }

    @Test
    public void getAuthorisationSubResources_success() {
        // When
        Optional<Xs2aAuthorisationSubResources> actualResponse = oauthPisScaAuthorisationService.getAuthorisationSubResources(PAYMENT_ID);

        // Then
        assertThat(actualResponse.isPresent()).isFalse();
    }

    @Test
    public void getAuthorisationScaStatus_success() {
        // When
        Optional<ScaStatus> actualResponse = oauthPisScaAuthorisationService.getAuthorisationScaStatus(PAYMENT_ID, AUTHORISATION_ID);

        // Then
        assertThat(actualResponse.isPresent()).isFalse();
    }

    @Test
    public void getCancellationAuthorisationScaStatus_success() {
        // When
        Optional<ScaStatus> actualResponse = oauthPisScaAuthorisationService.getCancellationAuthorisationScaStatus(PAYMENT_ID, CANCELLATION_AUTHORISATION_ID);

        // Then
        assertThat(actualResponse.isPresent()).isFalse();
    }

    @Test
    public void getScaApproachServiceType_success() {
        //When
        ScaApproach actualResponse = oauthPisScaAuthorisationService.getScaApproachServiceType();

        //Then
        assertThat(actualResponse).isNotNull();
    }
}
