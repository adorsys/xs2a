/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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

import de.adorsys.psd2.consent.api.ActionStatus;
import de.adorsys.psd2.consent.api.ais.AisConsentActionRequest;
import de.adorsys.psd2.consent.domain.account.AisConsentUsage;
import de.adorsys.psd2.consent.domain.consent.ConsentEntity;
import de.adorsys.psd2.consent.repository.AisConsentUsageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AisConsentUsageServiceTest {
    @InjectMocks
    private AisConsentUsageService aisConsentUsageService;
    @Mock
    private AisConsentUsageRepository aisConsentUsageRepository;

    @Test
    void incrementUsage_getUsageFromRepository() {
        //Given
        ConsentEntity consentEntity = new ConsentEntity();
        int initialUsage = 5;
        AisConsentActionRequest aisConsentActionRequest = new AisConsentActionRequest("tppId", "consentId", ActionStatus.SUCCESS, "requestUri", true, "resourceId", "transactionId");
        ArgumentCaptor<AisConsentUsage> argumentCaptor = ArgumentCaptor.forClass(AisConsentUsage.class);
        AisConsentUsage aisConsentUsage = new AisConsentUsage();
        aisConsentUsage.setUsage(initialUsage);
        when(aisConsentUsageRepository.findWriteByConsentAndUsageDateAndRequestUri(eq(consentEntity), any(LocalDate.class), eq(aisConsentActionRequest.getRequestUri()))).thenReturn(Optional.of(aisConsentUsage));
        //When
        aisConsentUsageService.incrementUsage(consentEntity, aisConsentActionRequest);
        //Then
        verify(aisConsentUsageRepository).save(argumentCaptor.capture());
        AisConsentUsage aisConsentUsageCaptured = argumentCaptor.getValue();
        assertEquals(++initialUsage, aisConsentUsageCaptured.getUsage());
        assertEquals(aisConsentActionRequest.getResourceId(), aisConsentUsageCaptured.getResourceId());
        assertEquals(aisConsentActionRequest.getTransactionId(), aisConsentUsageCaptured.getTransactionId());
    }

    @Test
    void incrementUsage_noUsageInRepository() {
        //Given
        ConsentEntity consentEntity = new ConsentEntity();
        AisConsentActionRequest aisConsentActionRequest = new AisConsentActionRequest("tppId", "consentId", ActionStatus.SUCCESS, "requestUri", true, "resourceId", "transactionId");
        ArgumentCaptor<AisConsentUsage> argumentCaptor = ArgumentCaptor.forClass(AisConsentUsage.class);
        when(aisConsentUsageRepository.findWriteByConsentAndUsageDateAndRequestUri(eq(consentEntity), any(LocalDate.class), eq(aisConsentActionRequest.getRequestUri()))).thenReturn(Optional.empty());
        //When
        aisConsentUsageService.incrementUsage(consentEntity, aisConsentActionRequest);
        //Then
        verify(aisConsentUsageRepository).save(argumentCaptor.capture());
        AisConsentUsage aisConsentUsageCaptured = argumentCaptor.getValue();
        assertEquals(1, aisConsentUsageCaptured.getUsage());
        assertEquals(aisConsentActionRequest.getResourceId(), aisConsentUsageCaptured.getResourceId());
        assertEquals(aisConsentActionRequest.getTransactionId(), aisConsentUsageCaptured.getTransactionId());
    }

    @Test
    void resetUsage() {
        //Given
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<AisConsentUsage>> argumentCaptor = ArgumentCaptor.forClass(List.class);
        ConsentEntity consentEntity = new ConsentEntity();
        List<AisConsentUsage> aisConsentUsageList = Arrays.asList(buildAisConsentUsage(5), buildAisConsentUsage(8), buildAisConsentUsage(10));
        when(aisConsentUsageRepository.findReadByConsentAndUsageDate(eq(consentEntity), any(LocalDate.class))).thenReturn(aisConsentUsageList);
        //When
        aisConsentUsageService.resetUsage(consentEntity);
        //Then
        verify(aisConsentUsageRepository).saveAll(argumentCaptor.capture());
        List<AisConsentUsage> aisConsentUsageListCaptured = argumentCaptor.getValue();
        assertEquals(aisConsentUsageList.size(), aisConsentUsageListCaptured.size());
        assertEquals(aisConsentUsageList.size(), aisConsentUsageListCaptured.stream().map(AisConsentUsage::getUsage).filter(usage -> usage == 0).count());
    }

    @Test
    void getUsageCounterMap() {
        //Given
        int frequencyPerDay = 30;
        ConsentEntity consentEntity = new ConsentEntity();
        consentEntity.setFrequencyPerDay(frequencyPerDay);
        List<AisConsentUsage> aisConsentUsageList = Arrays.asList(buildAisConsentUsage(5, "uri_1"), buildAisConsentUsage(8, "uri_2"), buildAisConsentUsage(10, "uri_3"));
        when(aisConsentUsageRepository.findReadByConsentAndUsageDate(eq(consentEntity), any(LocalDate.class))).thenReturn(aisConsentUsageList);
        //When
        Map<String, Integer> usageCounterMap = aisConsentUsageService.getUsageCounterMap(consentEntity);
        //Then
        aisConsentUsageList.forEach(aisConsentUsage -> assertEquals(frequencyPerDay - aisConsentUsage.getUsage(), usageCounterMap.get(aisConsentUsage.getRequestUri())));
    }

    private AisConsentUsage buildAisConsentUsage(int usage) {
        AisConsentUsage aisConsentUsage = new AisConsentUsage();
        aisConsentUsage.setUsage(usage);
        return aisConsentUsage;
    }

    private AisConsentUsage buildAisConsentUsage(int usage, String requestUri) {
        AisConsentUsage aisConsentUsage = new AisConsentUsage();
        aisConsentUsage.setUsage(usage);
        aisConsentUsage.setRequestUri(requestUri);
        return aisConsentUsage;
    }
}
