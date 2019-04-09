package de.adorsys.psd2.xs2a.service.authorization.ais;

import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.consent.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class OauthAisAuthorizationServiceTest {
    private static final String CONSENT_ID = "c966f143-f6a2-41db-9036-8abaeeef3af7";
    private static final String AUTHORISATION_ID = "ad746cb3-a01b-4196-a6b9-40b0e4cd2350";
    private static final UpdateConsentPsuDataReq UPDATE_CONSENT_PSU_DATA_REQ = new UpdateConsentPsuDataReq();
    private static final AccountConsentAuthorization ACCOUNT_CONSENT_AUTHORIZATION = new AccountConsentAuthorization();
    private static final UpdateConsentPsuDataResponse UPDATE_CONSENT_PSU_DATA_RESPONSE = new UpdateConsentPsuDataResponse();
    private static final PsuIdData PSU_ID_DATA = new PsuIdData("Test psuId", null, null, null);

    @InjectMocks
    private OauthAisAuthorizationService oauthAisAuthorizationService;

    @Test
    public void createConsentAuthorization_success() {
        //When
        Optional<CreateConsentAuthorizationResponse> actualResponse = oauthAisAuthorizationService.createConsentAuthorization(PSU_ID_DATA, CONSENT_ID);

        //Then
        assertThat(actualResponse.isPresent()).isFalse();
    }

    @Test
    public void updateConsentPsuData_success() {
        //When
        UpdateConsentPsuDataResponse actualResponse = oauthAisAuthorizationService.updateConsentPsuData(UPDATE_CONSENT_PSU_DATA_REQ, ACCOUNT_CONSENT_AUTHORIZATION);

        //Then
        assertThat(actualResponse).isEqualTo(UPDATE_CONSENT_PSU_DATA_RESPONSE);
    }

    @Test
    public void getAccountConsentAuthorizationById_success() {
        //When
        Optional<AccountConsentAuthorization>  actualResponse = oauthAisAuthorizationService.getAccountConsentAuthorizationById(AUTHORISATION_ID, CONSENT_ID);

        //Then
        assertThat(actualResponse.isPresent()).isFalse();
    }

    @Test
    public void getAuthorisationSubResources_success() {
        //When
        Optional<Xs2aAuthorisationSubResources> actualResponse = oauthAisAuthorizationService.getAuthorisationSubResources(CONSENT_ID);

        //Then
        assertThat(actualResponse.isPresent()).isFalse();
    }

    @Test
    public void getAuthorisationScaStatus_success() {
        //When
        Optional<ScaStatus> actualResponse = oauthAisAuthorizationService.getAuthorisationScaStatus(CONSENT_ID, AUTHORISATION_ID);

        //Then
        assertThat(actualResponse.isPresent()).isFalse();
    }

    @Test
    public void getScaApproachServiceType_success() {
        //When
        ScaApproach actualResponse = oauthAisAuthorizationService.getScaApproachServiceType();

        //Then
        assertThat(actualResponse).isNotNull();
    }
}
