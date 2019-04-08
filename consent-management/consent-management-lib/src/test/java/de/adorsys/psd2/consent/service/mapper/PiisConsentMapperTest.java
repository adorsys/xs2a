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

package de.adorsys.psd2.consent.service.mapper;

import de.adorsys.psd2.consent.domain.AccountReferenceEntity;
import de.adorsys.psd2.consent.domain.PsuData;
import de.adorsys.psd2.consent.domain.TppInfoEntity;
import de.adorsys.psd2.consent.domain.piis.PiisConsentEntity;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.piis.PiisConsent;
import de.adorsys.psd2.xs2a.core.piis.PiisConsentTppAccessType;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PiisConsentMapperTest {
    private static final String INSTANCE_ID = "823648238476238462";
    private static final String PSU_ID = "PSU-ID";
    private static final String TPP_AUTHORISATION_NUMBER = "authorisation number";
    private static final String TPP_AUTHORITY_ID = "authority id";
    private static final String EXTERNAL_ID = "83e42eb5-c800-4cdd-af1a-65c5c472317f";
    private static final OffsetDateTime REQUEST_DATETIME = OffsetDateTime.now().plusDays(1);
    private static final LocalDate LAST_ACTION_DATE = LocalDate.now();
    private static final LocalDate EXPIRY_DATE = LocalDate.now().plusDays(10);
    private static final int ALLOWED_FREQUENCY_PER_DAY = 4;
    private static final ConsentStatus CONSENT_STATUS = ConsentStatus.RECEIVED;
    private static final OffsetDateTime CREATION_TIMESTAMP = OffsetDateTime.now().plusDays(2);
    private static final String CARD_NUMBER = "1234567891234";
    private static final LocalDate CARD_EXPIRY_DATE = LocalDate.now().plusDays(1);
    private static final String CARD_INFORMATION = "MyMerchant Loyalty Card";
    private static final String REGISTRATION_INFORMATION = "Your contract Number 1234 with MyMerchant is completed with the registration with your bank.";
    private static final OffsetDateTime STATUS_CHANGE_TIMESTAMP = OffsetDateTime.now();

    @InjectMocks
    private PiisConsentMapper piisConsentMapper;
    @Mock
    private PsuDataMapper psuDataMapper;
    @Mock
    private TppInfoMapper tppInfoMapper;
    @Mock
    private AccountReferenceMapper accountReferenceMapper;

    @Before
    public void setUp() {
        when(psuDataMapper.mapToPsuIdData(buildPsuData())).thenReturn(buildPsuIdData());
        when(tppInfoMapper.mapToTppInfo(buildTppInfoEntity())).thenReturn(buildTppInfo());
        when(accountReferenceMapper.mapToAccountReferenceList(buildAccountReferenceEntityList())).thenReturn(buildAccountReferenceList());
    }

    @Test
    public void mapToPiisConsentListSuccess() {
        //Given
        PiisConsentEntity piisConsentEntity = buildPiisConsentEntity();
        List<PiisConsentEntity> piisConsentEntityList = Collections.singletonList(piisConsentEntity);

        //When
        List<PiisConsent> consents = piisConsentMapper.mapToPiisConsentList(piisConsentEntityList);

        //Then
        assertEquals(consents.size(), piisConsentEntityList.size());
        PiisConsent piisConsent = consents.get(0);

        Assert.assertEquals(piisConsentEntity.getExternalId(), piisConsent.getId());
        Assert.assertTrue(piisConsent.isRecurringIndicator());
        Assert.assertEquals(piisConsentEntity.getRequestDateTime(), piisConsent.getRequestDateTime());
        Assert.assertEquals(piisConsentEntity.getLastActionDate(), piisConsent.getLastActionDate());
        Assert.assertEquals(piisConsentEntity.getExpireDate(), piisConsent.getExpireDate());
        Assert.assertEquals(buildPsuIdData(), piisConsent.getPsuData());
        Assert.assertEquals(buildTppInfo(), piisConsent.getTppInfo());
        Assert.assertEquals(piisConsentEntity.getConsentStatus(), piisConsent.getConsentStatus());
        Assert.assertEquals(buildAccountReferenceList(), piisConsent.getAccounts());
        Assert.assertEquals(piisConsentEntity.getTppAccessType(), piisConsent.getTppAccessType());
        Assert.assertEquals(piisConsentEntity.getAllowedFrequencyPerDay(), piisConsent.getAllowedFrequencyPerDay());
        Assert.assertEquals(piisConsentEntity.getCreationTimestamp(), piisConsent.getCreationTimestamp());
        Assert.assertEquals(piisConsentEntity.getInstanceId(), piisConsent.getInstanceId());
        Assert.assertEquals(piisConsentEntity.getCardNumber(), piisConsent.getCardNumber());
        Assert.assertEquals(piisConsentEntity.getCardExpiryDate(), piisConsent.getCardExpiryDate());
        Assert.assertEquals(piisConsentEntity.getCardInformation(), piisConsent.getCardInformation());
        Assert.assertEquals(piisConsentEntity.getRegistrationInformation(), piisConsent.getRegistrationInformation());
        Assert.assertEquals(piisConsentEntity.getStatusChangeTimestamp(), piisConsent.getStatusChangeTimestamp());
    }

    @NotNull
    private PiisConsentEntity buildPiisConsentEntity() {
        PiisConsentEntity piisConsentEntity = new PiisConsentEntity();
        piisConsentEntity.setInstanceId(INSTANCE_ID);
        piisConsentEntity.setExternalId(EXTERNAL_ID);
        piisConsentEntity.setRecurringIndicator(true);
        piisConsentEntity.setRequestDateTime(REQUEST_DATETIME);
        piisConsentEntity.setLastActionDate(LAST_ACTION_DATE);
        piisConsentEntity.setExpireDate(EXPIRY_DATE);
        piisConsentEntity.setPsuData(buildPsuData());
        piisConsentEntity.setTppInfo(buildTppInfoEntity());
        piisConsentEntity.setConsentStatus(CONSENT_STATUS);
        piisConsentEntity.setAccounts(buildAccountReferenceEntityList());
        piisConsentEntity.setTppAccessType(PiisConsentTppAccessType.SINGLE_TPP);
        piisConsentEntity.setAllowedFrequencyPerDay(ALLOWED_FREQUENCY_PER_DAY);
        piisConsentEntity.setCreationTimestamp(CREATION_TIMESTAMP);
        piisConsentEntity.setCardNumber(CARD_NUMBER);
        piisConsentEntity.setCardExpiryDate(CARD_EXPIRY_DATE);
        piisConsentEntity.setCardInformation(CARD_INFORMATION);
        piisConsentEntity.setRegistrationInformation(REGISTRATION_INFORMATION);
        piisConsentEntity.setStatusChangeTimestamp(STATUS_CHANGE_TIMESTAMP);
        return piisConsentEntity;
    }

    private TppInfo buildTppInfo() {
        TppInfo tppInfo = new TppInfo();
        tppInfo.setAuthorisationNumber(TPP_AUTHORISATION_NUMBER);
        tppInfo.setAuthorityId(TPP_AUTHORITY_ID);
        return tppInfo;
    }

    private TppInfoEntity buildTppInfoEntity() {
        TppInfo tppInfo = buildTppInfo();
        TppInfoEntity tppInfoEntity = new TppInfoEntity();
        tppInfoEntity.setAuthorisationNumber(tppInfo.getAuthorisationNumber());
        tppInfoEntity.setAuthorityId(tppInfo.getAuthorityId());
        return tppInfoEntity;
    }

    private PsuIdData buildPsuIdData() {
        return new PsuIdData(PSU_ID, null, null, null);
    }

    private PsuData buildPsuData() {
        PsuIdData psuIdData = buildPsuIdData();
        return new PsuData(psuIdData.getPsuId(), psuIdData.getPsuIdType(), psuIdData.getPsuCorporateId(), psuIdData.getPsuCorporateIdType());
    }

    private AccountReference buildAccountReference() {
        return new AccountReference("aspspAccountId", "resourceId",
                                    "DE89370400440532013000",
                                    null,
                                    null,
                                    null,
                                    null,
                                    null);
    }

    private List<AccountReference> buildAccountReferenceList() {
        return Collections.singletonList(buildAccountReference());
    }

    private List<AccountReferenceEntity> buildAccountReferenceEntityList() {
        return Collections.singletonList(buildAccountReferenceEntity());
    }

    private AccountReferenceEntity buildAccountReferenceEntity() {
        AccountReference accountReference = buildAccountReference();
        AccountReferenceEntity accountReferenceEntity = new AccountReferenceEntity();
        accountReferenceEntity.setAspspAccountId(accountReference.getAspspAccountId());
        accountReferenceEntity.setIban(accountReference.getIban());
        return accountReferenceEntity;
    }
}
