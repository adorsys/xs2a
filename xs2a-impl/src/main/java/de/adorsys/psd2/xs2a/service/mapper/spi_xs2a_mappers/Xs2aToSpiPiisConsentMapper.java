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

package de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers;

import de.adorsys.psd2.core.data.piis.v1.PiisConsent;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.domain.authorisation.CommonAuthorisationParameters;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiScaConfirmation;
import de.adorsys.psd2.xs2a.spi.domain.piis.SpiPiisConsent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Optional;

@Mapper(componentModel = "spring", uses = {Xs2aToSpiPsuDataMapper.class})
public interface Xs2aToSpiPiisConsentMapper {

    @Mapping(target = "account", expression = "java(toSpiAccountReference(piisConsent.getAccountReference()))")
    @Mapping(target = "cardExpiryDate", source = "consentData.cardExpiryDate")
    @Mapping(target = "cardInformation", source = "consentData.cardInformation")
    @Mapping(target = "cardNumber", source = "consentData.cardNumber")
    @Mapping(target = "psuData", source = "psuIdDataList")
    @Mapping(target = "registrationInformation", source = "consentData.registrationInformation")
    @Mapping(target = "requestDateTime", source = "creationTimestamp")
    @Mapping(target = "tppAuthorisationNumber", source = "consentTppInformation.tppInfo.authorisationNumber")
    SpiPiisConsent mapToSpiPiisConsent(PiisConsent piisConsent);

    default SpiScaConfirmation toSpiScaConfirmation(CommonAuthorisationParameters request, PsuIdData psuData) {
        SpiScaConfirmation accountConfirmation = new SpiScaConfirmation();
        accountConfirmation.setConsentId(request.getBusinessObjectId());
        accountConfirmation.setPsuId(Optional.ofNullable(psuData).map(PsuIdData::getPsuId).orElse(null));
        accountConfirmation.setTanNumber(request.getScaAuthenticationData());
        return accountConfirmation;
    }

    default SpiAccountReference toSpiAccountReference(AccountReference account) {
        return new SpiAccountReference(
            account.getAspspAccountId(),
            account.getResourceId(),
            account.getIban(),
            account.getBban(),
            account.getPan(),
            account.getMaskedPan(),
            account.getMsisdn(),
            account.getCurrency(),
            account.getOther());
    }
}
