/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.aspsp.xs2a.service.authorization.ais.stage;

/*
@RunWith(MockitoJUnitRunner.class)
public class AisScaAuthenticatedStageTest {
    private static final String CONSENT_ID = "Test consentId";
    private static final ConsentStatus VALID_CONSENT_STATUS = ConsentStatus.VALID;
    private static final String TEST_AUTHENTICATION_DATA = "Test authenticationData";
    private static final ScaStatus FINALIZED_SCA_STATUS = ScaStatus.FINALISED;
    private static final SpiResponseStatus RESPONSE_STATUS = SpiResponseStatus.LOGICAL_FAILURE;
    private static final MessageErrorCode ERROR_CODE = MessageErrorCode.FORMAT_ERROR;
    private static final SpiPsuData SPI_PSU_DATA = new SpiPsuData(null, null, null, null);
    private static final AspspConsentData ASPSP_CONSENT_DATA = new AspspConsentData(new byte[0], "Some Consent ID");

    @InjectMocks
    private AisScaAuthenticatedStage scaAuthenticatedStage;

    @Mock
    private Xs2aAisConsentService aisConsentService;
    @Mock
    private AisConsentDataService aisConsentDataService;
    @Mock
    private AisConsentSpi aisConsentSpi;
    @Mock
    private Xs2aAisConsentMapper aisConsentMapper;
    @Mock
    private SpiResponseStatusToXs2aMessageErrorCodeMapper messageErrorCodeMapper;
    @Mock
    private Xs2aToSpiPsuDataMapper psuDataMapper;
    @Mock
    private UpdateConsentPsuDataReq request;
    @Mock
    private SpiAccountConsent accountConsent;
    @Mock
    private SpiScaConfirmation scaConfirmation;

    @Before
    public void setUp() {
        when(request.getConsentId())
            .thenReturn(CONSENT_ID);

        when(psuDataMapper.mapToSpiPsuData(any(PsuIdData.class)))
            .thenReturn(SPI_PSU_DATA);

        when(aisConsentMapper.mapToSpiScaConfirmation(request))
            .thenReturn(scaConfirmation);

        when(aisConsentDataService.getAspspConsentDataByConsentId(CONSENT_ID))
            .thenReturn(ASPSP_CONSENT_DATA);

        doNothing()
            .when(aisConsentDataService).updateAspspConsentData(ASPSP_CONSENT_DATA);
    }

    @Test
    public void apply_Success() {
        when(aisConsentSpi.verifyScaAuthorisation(SPI_PSU_DATA, scaConfirmation, accountConsent, ASPSP_CONSENT_DATA))
            .thenReturn(buildSuccessSpiResponse());

        doNothing()
            .when(aisConsentService).updateConsentStatus(CONSENT_ID, VALID_CONSENT_STATUS);

        when(request.getScaAuthenticationData())
            .thenReturn(TEST_AUTHENTICATION_DATA);

        UpdateConsentPsuDataResponse actualResponse = scaAuthenticatedStage.apply(request);

        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getScaAuthenticationData()).isEqualTo(TEST_AUTHENTICATION_DATA);
        assertThat(actualResponse.getScaStatus()).isEqualTo(FINALIZED_SCA_STATUS);
        assertThat(actualResponse.getResponseLinkType()).isEqualTo(START_AUTHORISATION_WITH_AUTHENTICATION_METHOD_SELECTION);
    }

    @Test
    public void apply_Failure_SpiResponseWithError() {
        when(aisConsentSpi.verifyScaAuthorisation(SPI_PSU_DATA, scaConfirmation, accountConsent, ASPSP_CONSENT_DATA))
            .thenReturn(buildErrorSpiResponse());

        when(messageErrorCodeMapper.mapToMessageErrorCode(RESPONSE_STATUS))
            .thenReturn(ERROR_CODE);

        UpdateConsentPsuDataResponse actualResponse = scaAuthenticatedStage.apply(request);

        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getErrorCode()).isEqualTo(ERROR_CODE);
    }

    // Needed because SpiResponse is final, so it's impossible to mock it
    private SpiResponse<VoidResponse> buildSuccessSpiResponse() {
        return SpiResponse.<VoidResponse>builder()
                   .payload(SpiResponse.voidResponse())
                   .aspspConsentData(ASPSP_CONSENT_DATA)
                   .success();
    }

    // Needed because SpiResponse is final, so it's impossible to mock it
    private SpiResponse<VoidResponse> buildErrorSpiResponse() {
        return SpiResponse.<VoidResponse>builder()
                   .payload(SpiResponse.voidResponse())
                   .aspspConsentData(ASPSP_CONSENT_DATA)
                   .fail(RESPONSE_STATUS);
    }
}
*/
