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

package de.adorsys.psd2.consent.service;

import de.adorsys.psd2.consent.api.AspspDataService;
import de.adorsys.psd2.consent.api.CmsAspspConsentDataBase64;
import de.adorsys.psd2.consent.api.ConsentType;
import de.adorsys.psd2.xs2a.core.consent.AspspConsentData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CommonConsentServiceInternalTest {
    @InjectMocks
    private CommonConsentServiceInternal commonConsentService;
    @Mock
    private AspspDataService aspspDataService;
    @Mock
    private AisConsentServiceInternal aisConsentService;
    @Mock
    private PisConsentServiceInternal pisConsentService;
    @Mock
    private PiisConsentServiceInternal piisConsentService;

    private static CmsAspspConsentDataBase64 CMS_ASPSP_CONSENT_DATA_BASE_64, CMS_ASPSP_CONSENT_DATA_BASE_64_NO_CONSENT_DATA;
    private static final String EXTERNAL_CONSENT_ID = "4b112130-6a96-4941-a220-2da8a4af2c65";
    private static final String EXTERNAL_CONSENT_NO_CONSENT_DATA = "d2796b05-418e-49bc-84ce-c6728a1b2018";
    private static final String EXTERNAL_CONSENT_ID_NOT_EXIST = "4b112130-6a96-4941-a220-2da8a4af2c63";
    private static final String PAYMENT_ID = "5bbde955ca10e8e4035a10c2";
    private static final String PAYMENT_ID_NOT_EXIST = "5bbdcb28ca10e8e14a41b12f";
    private static final String CONSENT_DATA = "test data";
    private static final byte[] ENCRYPTED_CONSENT_DATA = CONSENT_DATA.getBytes();
    private static final String ENCRYPTED_CONSENT_DATA_BASE64_ENCODED = Base64.getEncoder().encodeToString(ENCRYPTED_CONSENT_DATA);

    @Before
    public void setUp() {
        AspspConsentData aspspConsentData = new AspspConsentData(ENCRYPTED_CONSENT_DATA, EXTERNAL_CONSENT_ID);
        AspspConsentData aspspConsentDataNoData = new AspspConsentData(null, EXTERNAL_CONSENT_ID);

        CMS_ASPSP_CONSENT_DATA_BASE_64 = new CmsAspspConsentDataBase64(EXTERNAL_CONSENT_ID, ENCRYPTED_CONSENT_DATA_BASE64_ENCODED);
        CMS_ASPSP_CONSENT_DATA_BASE_64_NO_CONSENT_DATA = new CmsAspspConsentDataBase64(EXTERNAL_CONSENT_ID, null);

        when(aisConsentService.isConsentExist(EXTERNAL_CONSENT_ID)).thenReturn(true);
        when(aisConsentService.isConsentExist(EXTERNAL_CONSENT_NO_CONSENT_DATA)).thenReturn(true);
        when(aisConsentService.isConsentExist(EXTERNAL_CONSENT_ID_NOT_EXIST)).thenReturn(false);

        when(aspspDataService.readAspspConsentData(EXTERNAL_CONSENT_ID)).thenReturn(Optional.of(aspspConsentData));
        when(aspspDataService.readAspspConsentData(EXTERNAL_CONSENT_NO_CONSENT_DATA)).thenReturn(Optional.of(aspspConsentDataNoData));
        when(aspspDataService.readAspspConsentData(PAYMENT_ID)).thenReturn(Optional.of(aspspConsentData));
        when(aspspDataService.updateAspspConsentData(aspspConsentData)).thenReturn(true);

        when(pisConsentService.isConsentExist(PAYMENT_ID)).thenReturn(true);
        when(pisConsentService.isConsentExist(PAYMENT_ID_NOT_EXIST)).thenReturn(false);
    }

    @Test
    public void checkExistingConsentFlow() {
        Arrays.asList(ConsentType.values())
            .forEach(consentType -> commonConsentService.getAspspConsentDataByConsentId(EXTERNAL_CONSENT_ID, consentType));
        Arrays.asList(aisConsentService, pisConsentService, piisConsentService)
            .forEach(service -> verify(service, times(1)).isConsentExist(EXTERNAL_CONSENT_ID));
    }

    @Test
    public void getAspspConsentDataByConsentIdSuccess() {
        Optional<CmsAspspConsentDataBase64> cmsAspspConsentDataBase64 = commonConsentService.getAspspConsentDataByConsentId(EXTERNAL_CONSENT_ID, ConsentType.AIS);
        assertTrue(cmsAspspConsentDataBase64.isPresent());
        assertEquals(cmsAspspConsentDataBase64.get().getAspspConsentDataBase64(), ENCRYPTED_CONSENT_DATA_BASE64_ENCODED);
    }

    @Test
    public void getAspspConsentDataByConsentIdFail() {
        Optional<CmsAspspConsentDataBase64> cmsAspspConsentDataBase64 = commonConsentService.getAspspConsentDataByConsentId(EXTERNAL_CONSENT_ID_NOT_EXIST, ConsentType.AIS);
        assertFalse(cmsAspspConsentDataBase64.isPresent());
    }

    @Test
    public void getAspspConsentDataByPaymentIdSuccess() {
        Optional<CmsAspspConsentDataBase64> cmsAspspConsentDataBase64 = commonConsentService.getAspspConsentDataByPaymentId(PAYMENT_ID);
        assertTrue(cmsAspspConsentDataBase64.isPresent());
        assertEquals(cmsAspspConsentDataBase64.get().getAspspConsentDataBase64(), ENCRYPTED_CONSENT_DATA_BASE64_ENCODED);
    }

    @Test
    public void getAspspConsentDataByPaymentIdFail() {
        Optional<CmsAspspConsentDataBase64> cmsAspspConsentDataBase64 = commonConsentService.getAspspConsentDataByPaymentId(PAYMENT_ID_NOT_EXIST);
        assertFalse(cmsAspspConsentDataBase64.isPresent());
    }

    @Test
    public void saveAspspConsentDataSuccess() {
        Optional<String> consentId = commonConsentService.saveAspspConsentData(EXTERNAL_CONSENT_ID, CMS_ASPSP_CONSENT_DATA_BASE_64, ConsentType.AIS);
        assertTrue(consentId.isPresent());
        assertEquals(consentId.get(), EXTERNAL_CONSENT_ID);
    }

    @Test
    public void saveAspspConsentDataFail() {
        Optional<String> consentId = commonConsentService.saveAspspConsentData(EXTERNAL_CONSENT_ID_NOT_EXIST, CMS_ASPSP_CONSENT_DATA_BASE_64, ConsentType.AIS);
        assertFalse(consentId.isPresent());
    }

    @Test
    public void getAspspConsentDataByConsentIdNoConsentData() {
        Optional<CmsAspspConsentDataBase64> cmsAspspConsentDataBase64 = commonConsentService.getAspspConsentDataByConsentId(EXTERNAL_CONSENT_NO_CONSENT_DATA, ConsentType.AIS);
        assertTrue(cmsAspspConsentDataBase64.isPresent());
        assertNull(cmsAspspConsentDataBase64.get().getAspspConsentDataBase64());
    }

    @Test
    public void saveAspspConsentDataNoConsentData() {
        Optional<String> consentId = commonConsentService.saveAspspConsentData(EXTERNAL_CONSENT_NO_CONSENT_DATA, CMS_ASPSP_CONSENT_DATA_BASE_64_NO_CONSENT_DATA, ConsentType.AIS);
        assertFalse(consentId.isPresent());
    }
}
