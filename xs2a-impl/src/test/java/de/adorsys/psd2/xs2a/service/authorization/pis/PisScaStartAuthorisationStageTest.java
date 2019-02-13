/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.service.authorization.pis;

import de.adorsys.psd2.consent.api.pis.authorisation.GetPisAuthorisationResponse;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.MessageErrorCode;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataRequest;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.authorization.pis.stage.PisScaStartAuthorisationStage;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PisScaStartAuthorisationStageTest {
    private static final String PSU_ID = "Test psuId";
    private static final PsuIdData PSU_ID_DATA = new PsuIdData(PSU_ID, null, null, null);

    @InjectMocks
    private PisScaStartAuthorisationStage pisScaStartAuthorisationStage;
    @Mock
    private Xs2aUpdatePisCommonPaymentPsuDataRequest xs2aUpdatePisCommonPaymentPsuDataRequest;
    @Mock
    private GetPisAuthorisationResponse getPisAuthorisationResponse;

    @Test
    public void apply_Identification_Success() {
        //Given
        when(xs2aUpdatePisCommonPaymentPsuDataRequest.isUpdatePsuIdentification()).thenReturn(true);
        when(xs2aUpdatePisCommonPaymentPsuDataRequest.getPsuData()).thenReturn(PSU_ID_DATA);
        //When
        Xs2aUpdatePisCommonPaymentPsuDataResponse response = pisScaStartAuthorisationStage.apply(xs2aUpdatePisCommonPaymentPsuDataRequest, getPisAuthorisationResponse);
        //Then
        assertThat(response.getScaStatus()).isEqualTo(ScaStatus.PSUIDENTIFIED);
        assertThat(response.getPsuId()).isEqualTo(PSU_ID);
    }

    @Test
    public void apply_Identification_Failure() {
        //Given
        when(xs2aUpdatePisCommonPaymentPsuDataRequest.isUpdatePsuIdentification()).thenReturn(true);
        when(xs2aUpdatePisCommonPaymentPsuDataRequest.getPsuData()).thenReturn(null);
        //When
        Xs2aUpdatePisCommonPaymentPsuDataResponse response = pisScaStartAuthorisationStage.apply(xs2aUpdatePisCommonPaymentPsuDataRequest, getPisAuthorisationResponse);
        //Then
        assertThat(response.getScaStatus()).isEqualTo(ScaStatus.FAILED);
        assertThat(response.getErrorHolder().getErrorType()).isEqualTo(ErrorType.PIS_400);
        assertThat(response.getErrorHolder().getErrorCode()).isEqualTo(MessageErrorCode.FORMAT_ERROR);
    }
}
