/*
 * Copyright 2018-2025 adorsys GmbH & Co KG
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
 * contact us at sales@adorsys.com.
 */

package de.adorsys.psd2.xs2a.web.mapper;

import de.adorsys.psd2.model.*;
import de.adorsys.psd2.xs2a.domain.vop.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ValueMapping;
import org.mapstruct.NullValueMappingStrategy;

import java.time.LocalDate;
import java.util.Optional;

/**
 * MapStruct mapper interface for VOP domain objects to PSD2 API response mapping (XS2A → PSD2 API).
 */
@Mapper(componentModel = "spring", nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
@SuppressWarnings({
    "PMD.TooManyMethods",
    "PMD.ShortMethodName",
    "PMD.CouplingBetweenObjects",
    "PMD.GodClass",
    "PMD.CyclomaticComplexity"
})
// Mapper interface with multiple small enum converters
public interface VopModelMapperPsd2 {

    /**
     * Maps a VopInitiationResponse domain object to a VerificationOfParty200Response API model.
     */
    VerificationOfParty200Response mapToVerificationOfParty200Response(VopInitiationResponse response);

    /**
     * Maps a VopInitiationResponse domain object to a BulkVerificationOfParty200Response API model.
     */
    BulkVerificationOfParty200Response mapToBulkVerificationOfParty200Response(VopInitiationResponse response);

    /**
     * Maps a core PartyVerificationResponse to an API PartyVerificationResponse.
     */
    PartyVerificationResponse mapPartyVerificationResponse(de.adorsys.psd2.xs2a.core.vop.PartyVerificationResponse coreResponse);

    /**
     * Maps a GetCommonVopResponse domain object to a GetBulkVerificationOfParty200Response API model.
     */
    @Mapping(target = "partyVerificationRequestsAndResults", source = "partyVerificationRequestResponses")
    GetBulkVerificationOfParty200Response mapToGetBulkVerificationOfParty200Response(GetCommonVopResponse response);

    /**
     * Maps a GetCommonVopResponse domain object to a GetBulkVerificationOfPartyStatus200Response API model.
     * This method maps the status information for bulk party verification requests.
     */
    GetBulkVerificationOfPartyStatus200Response mapToGetBulkVerificationOfPartyStatus200Response(GetCommonVopStatusResponse response);

    /**
     * Maps a core VopRequestAndResponse to an API PartyVerificationRequestAndResponse.
     */
    PartyVerificationRequestAndResponse mapVopRequestAndResponse(VopRequestAndResponse coreRequestResponse);

    /**
     * Maps enum PartyNameMatchCode from core to API model.
     */
    @ValueMapping(source = "MTCH", target = "MTCH")
    @ValueMapping(source = "NMTC", target = "NMTC")
    @ValueMapping(source = "CMTC", target = "CMTC")
    @ValueMapping(source = "NOAP", target = "NOAP")
    PartyNameMatchCode mapPartyNameMatchCode(de.adorsys.psd2.xs2a.core.vop.PartyNameMatchCode coreCode);

    /**
     * Maps enum PartyIdentificationMatchCode from core to API model.
     */
    @ValueMapping(source = "MTCH", target = "MTCH")
    @ValueMapping(source = "NMTC", target = "NMTC")
    @ValueMapping(source = "NOAP", target = "NOAP")
    PartyIdentificationMatchCode mapPartyIdentificationMatchCode(de.adorsys.psd2.xs2a.core.vop.PartyIdentificationMatchCode coreCode);

    /**
     * Maps enum VopBulkStatus from core to API model.
     */
    @ValueMapping(source = "RECEIVED", target = "RECIEVED")
    @ValueMapping(source = "PENDING", target = "PENDING")
    @ValueMapping(source = "REJECTED", target = "REJECTED")
    @ValueMapping(source = "COMPLETED", target = "COMPLETED")
    VOPBulkStatus mapVopBulkStatus(de.adorsys.psd2.xs2a.core.vop.VopBulkStatus coreStatus);

    /**
     * Maps a CreditorConfirmationResponse domain object to a CreditorConfirmation200Response API model.
     */
    CreditorConfirmation200Response mapToCreditorConfirmation200Response(CreditorConfirmationResponse response);

    /**
     * Maps domain Links to Links200CreditorConfirmation API model.
     */
    Links200CreditorConfirmation mapToLinks200CreditorConfirmation(de.adorsys.psd2.xs2a.domain.Links links);

    /**
     * Maps domain HrefType to API HrefType.
     */
    HrefType mapHrefType(de.adorsys.psd2.xs2a.domain.HrefType hrefType);

    /**
     * Maps core AccountReference to API AccountReference.
     */
    AccountReference mapAccountReference(de.adorsys.psd2.xs2a.core.vop.AccountReference coreAccountReference);

    /**
     * Maps core AgentDescription to API AgentDescription.
     */
    AgentDescription mapAgentDescription(de.adorsys.psd2.xs2a.core.vop.AgentDescription coreAgentDescription);

    /**
     * Maps core PartyDescription4 to API PartyDescription4.
     */
    PartyDescription4 mapPartyDescription(de.adorsys.psd2.xs2a.core.vop.PartyDescription4 corePartyDescription);

    /**
     * Maps core PartyIdentification to API PartyIdentification.
     */
    PartyIdentification mapPartyIdentification(de.adorsys.psd2.xs2a.core.vop.PartyIdentification coreIdentification);

    /**
     * Maps core OrganisationIdentification to API OrganisationIdentification.
     */
    OrganisationIdentification mapOrganisationIdentification(de.adorsys.psd2.xs2a.core.vop.OrganisationIdentification coreOrgId);

    /**
     * Maps core GenericOrganisationIdentification to API GenericOrganisationIdentification.
     */
    GenericOrganisationIdentification mapGenericOrganisationIdentification(de.adorsys.psd2.xs2a.core.vop.GenericOrganisationIdentification coreGenericOrgId);

    /**
     * Maps core PrivateIdentification to API PrivateIdentification.
     */
    PrivateIdentification mapPrivateIdentification(de.adorsys.psd2.xs2a.core.vop.PrivateIdentification corePrivateId);

    /**
     * Maps core PersonIdentification to API PersonIdentification.
     */
    PersonIdentification mapPersonIdentification(de.adorsys.psd2.xs2a.core.vop.PersonIdentification corePersonId);

    /**
     * Maps core FinancialInstitutionIdentification to API FinancialInstitutionIdentification.
     */
    FinancialInstitutionIdentification mapFinancialInstitutionIdentification(de.adorsys.psd2.xs2a.core.vop.FinancialInstitutionIdentification coreFiId);

    /**
     * Maps core ClearingSystemMemberIdentification to API ClearingSystemMemberIdentification.
     */
    ClearingSystemMemberIdentification mapClearingSystemMemberIdentification(de.adorsys.psd2.xs2a.core.vop.ClearingSystemMemberIdentification coreCsmId);

    /**
     * Maps core GenericFinancialInstitutionIdentification to API GenericFinancialInstitutionIdentification.
     */
    GenericFinancialInstitutionIdentification mapGenericFinancialInstitutionIdentification(de.adorsys.psd2.xs2a.core.vop.GenericFinancialInstitutionIdentification coreGenFiId);

    /**
     * Maps core PostalAddress to API PostalAddress.
     */
    PostalAddress mapPostalAddress(de.adorsys.psd2.xs2a.core.vop.PostalAddress coreAddress);

    // ===============================================
    // Helper methods for Optional mapping
    // ===============================================

    /**
     * Wraps a String in Optional for API models.
     */
    default Optional<String> map(String value) {
        return Optional.ofNullable(value);
    }

    /**
     * Wraps a LocalDate in Optional for API models.
     */
    default Optional<LocalDate> map(LocalDate value) {
        return Optional.ofNullable(value);
    }

    /**
     * Wraps an HrefType in Optional for API models.
     */
    default Optional<HrefType> map(HrefType value) {
        return Optional.ofNullable(value);
    }

    /**
     * Wraps a PartyIdentification in Optional for API models.
     */
    default Optional<PartyIdentification> map(PartyIdentification value) {
        return Optional.ofNullable(value);
    }

    /**
     * Wraps an OrganisationIdentification in Optional for API models.
     */
    default Optional<OrganisationIdentification> map(OrganisationIdentification value) {
        return Optional.ofNullable(value);
    }

    /**
     * Wraps a PrivateIdentification in Optional for API models.
     */
    default Optional<PrivateIdentification> map(PrivateIdentification value) {
        return Optional.ofNullable(value);
    }

    /**
     * Wraps a ClearingSystemMemberIdentification in Optional for API models.
     */
    default Optional<ClearingSystemMemberIdentification> map(ClearingSystemMemberIdentification value) {
        return Optional.ofNullable(value);
    }

    /**
     * Wraps a PostalAddress in Optional for API models.
     */
    default Optional<PostalAddress> map(PostalAddress value) {
        return Optional.ofNullable(value);
    }

    /**
     * Wraps a GenericFinancialInstitutionIdentification in Optional for API models.
     */
    default Optional<GenericFinancialInstitutionIdentification> map(GenericFinancialInstitutionIdentification value) {
        return Optional.ofNullable(value);
    }

    /**
     * Wraps a PartyNameMatchCode in Optional for API models.
     */
    default Optional<PartyNameMatchCode> map(PartyNameMatchCode value) {
        return Optional.ofNullable(value);
    }

    /**
     * Wraps a PartyIdentificationMatchCode in Optional for API models.
     */
    default Optional<PartyIdentificationMatchCode> map(PartyIdentificationMatchCode value) {
        return Optional.ofNullable(value);
    }

    /**
     * Wraps an AgentDescription in Optional for API models.
     */
    default Optional<AgentDescription> map(AgentDescription value) {
        return Optional.ofNullable(value);
    }

    /**
     * Wraps a StatusReasonCode in Optional for API models.
     */
    default Optional<StatusReasonCode> map(StatusReasonCode value) {
        return Optional.ofNullable(value);
    }

    /**
     * Maps String challengeData to Optional&lt;Challenge&gt;.
     *
     * @param challengeData the challenge data string to map
     * @return an Optional containing the Challenge if data is present, empty otherwise
     */
    default Optional<Challenge> mapChallengeData(String challengeData) {
        if (challengeData == null || challengeData.isEmpty()) {
            return Optional.empty();
        }
        Challenge challenge = new Challenge();
        challenge.addDataItem(challengeData);
        return Optional.of(challenge);
    }

    /**
     * Maps String schemeNameCode to Optional&lt;OrganisationIdentificationCode&gt;.
     *
     * @param schemeNameCode the organisation scheme name code to map
     * @return an Optional containing the OrganisationIdentificationCode if present, empty otherwise
     */
    default Optional<OrganisationIdentificationCode> mapOrganisationSchemeNameCode(String schemeNameCode) {
        if (schemeNameCode == null) {
            return Optional.empty();
        }
        return Optional.of(OrganisationIdentificationCode.fromValue(schemeNameCode));
    }

    /**
     * Maps String schemeNameCode to Optional&lt;PersonIdentificationCode&gt;.
     *
     * @param schemeNameCode the person scheme name code to map
     * @return an Optional containing the PersonIdentificationCode if present, empty otherwise
     */
    default Optional<PersonIdentificationCode> mapPersonSchemeNameCode(String schemeNameCode) {
        if (schemeNameCode == null) {
            return Optional.empty();
        }
        return Optional.of(PersonIdentificationCode.fromValue(schemeNameCode));
    }

    /**
     * Maps String clearingSystemIdentificationCode to Optional&lt;ClearingSystemIdentificationCode&gt;.
     *
     * @param clearingSystemIdentificationCode the clearing system identification code to map
     * @return an Optional containing the ClearingSystemIdentificationCode if present, empty otherwise
     */
    default Optional<ClearingSystemIdentificationCode> mapClearingSystemIdentificationCode(String clearingSystemIdentificationCode) {
        if (clearingSystemIdentificationCode == null) {
            return Optional.empty();
        }
        return Optional.of(ClearingSystemIdentificationCode.fromValue(clearingSystemIdentificationCode));
    }
}
