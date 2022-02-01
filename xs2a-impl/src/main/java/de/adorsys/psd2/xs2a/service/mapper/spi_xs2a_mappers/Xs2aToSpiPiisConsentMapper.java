/*
 * Copyright 2018-2022 adorsys GmbH & Co KG
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version. This program is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 *
 * This project is also available under a separate commercial license. You can
 * contact us at psd2@adorsys.com.
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
