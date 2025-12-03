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

package de.adorsys.psd2.xs2a.service.mapper;

import de.adorsys.psd2.model.*;
import de.adorsys.psd2.xs2a.domain.vop.BulkVop;
import de.adorsys.psd2.xs2a.domain.vop.SingleVop;
import de.adorsys.psd2.xs2a.web.vop_mapper.VopAccountReferenceMapper;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

/**
 * MapStruct mapper for VOP API models to XS2A domain models.
 * Follows the pattern established in PaymentModelMapper for handling complex nested objects.
 *
 * <p>This mapper handles deeply nested Optional fields from the API layer and unwraps them
 * to plain types for the internal domain models. All mappings maintain null safety and
 * provide appropriate error handling.</p>
 */
@Mapper(componentModel = "spring",
    uses = {VopAccountReferenceMapper.class})
@SuppressWarnings("PMD")
public interface VopModelMapper {

    /**
     * Maps VOP request DTO to internal domain object.
     * Delegates complex nested object mappings to specialized mappers.
     *
     * @param request the VOP API request
     * @return the internal SingleVop domain object
     */
    @Mapping(target = "partyAccount", source = "partyAccount")
    @Mapping(target = "party", ignore = true)
    @Mapping(target = "partyAgent", ignore = true)
    @Mapping(target = "requestingAgent", ignore = true)
    @Mapping(target = "unstructuredRemittanceInformation", source = "unstructuredRemittanceInformation")
    @Mapping(target = "vopData", ignore = true)
    @Mapping(target = "creationTimestamp", ignore = true)
    @Mapping(target = "contentType", ignore = true)
    @Mapping(target = "instanceId", ignore = true)
    @Mapping(target = "customContentType", ignore = true)
    SingleVop mapToXs2aVop(VerificationOfPartyRequest request);

    /**
     * After mapping callback to handle Optional fields and complex nested objects.
     * This follows the pattern used in PaymentModelMapper's afterMapping method.
     *
     * <p>Mapping Strategy:</p>
     * <ul>
     *   <li>All Optional fields are unwrapped with null-safe checks</li>
     *   <li>Nested complex objects are recursively mapped</li>
     *   <li>Primitive types are directly assigned</li>
     *   <li>Collections are handled with stream operations where applicable</li>
     * </ul>
     *
     * @param request the API request with Optional fields
     * @param singleVop the target domain object to populate
     */
    @AfterMapping
    default void mapOptionalFields(VerificationOfPartyRequest request,
                                   @MappingTarget SingleVop singleVop) {
        // Map party (required but contains Optional fields)

            singleVop.setParty(mapPartyDescription(request.getParty()));


        // Handle Optional<AgentDescription> partyAgent
        request.getPartyAgent().ifPresent(agent ->
            singleVop.setPartyAgent(mapAgentDescription(agent))
        );

        // Handle Optional<AgentDescription> requestingAgent
        request.getRequestingAgent().ifPresent(agent ->
            singleVop.setRequestingAgent(mapAgentDescription(agent))
        );
    }

    /**
     * Maps bulk VOP request DTO to internal domain object.
     * Delegates complex nested object mappings to specialized mappers.
     *
     * @param request the bulk VOP API request
     * @return the internal BulkVop domain object
     */
    @Mapping(target = "partyVerificationRequests", ignore = true)
    @Mapping(target = "requestingAgent", ignore = true)
    @Mapping(target = "vopData", ignore = true)
    @Mapping(target = "creationTimestamp", ignore = true)
    @Mapping(target = "contentType", ignore = true)
    @Mapping(target = "instanceId", ignore = true)
    @Mapping(target = "customContentType", ignore = true)
    BulkVop mapToXs2aVop(BulkVerificationOfPartyRequest request);

    /**
     * Maps a single PartyVerificationRequest from API to internal model.
     * Handles Optional fields within the request.
     *
     * @param apiRequest the API party verification request
     * @return the internal party verification request
     */
    default de.adorsys.psd2.xs2a.core.vop.PartyVerificationRequest mapPartyVerificationRequest(
            de.adorsys.psd2.model.PartyVerificationRequest apiRequest) {
        if (apiRequest == null) {
            return null;
        }

        de.adorsys.psd2.xs2a.core.vop.PartyVerificationRequest xs2aRequest =
            new de.adorsys.psd2.xs2a.core.vop.PartyVerificationRequest();

        // Map basic fields
        xs2aRequest.setUetr(apiRequest.getUetr());

        // Map AccountReference

            de.adorsys.psd2.xs2a.core.vop.AccountReference xs2aAccount =
                new de.adorsys.psd2.xs2a.core.vop.AccountReference();
            xs2aAccount.setIban(apiRequest.getPartyAccount().getIban());
            xs2aAccount.setBban(apiRequest.getPartyAccount().getBban());
            xs2aAccount.setPan(apiRequest.getPartyAccount().getPan());
            xs2aAccount.setMaskedPan(apiRequest.getPartyAccount().getMaskedPan());
            xs2aAccount.setMsisdn(apiRequest.getPartyAccount().getMsisdn());
            xs2aAccount.setCurrency(apiRequest.getPartyAccount().getCurrency());
            xs2aRequest.setPartyAccount(xs2aAccount);


        // Map PartyDescription4
        xs2aRequest.setParty(mapPartyDescription(apiRequest.getParty()));

        // Handle Optional<AgentDescription> partyAgent
        apiRequest.getPartyAgent().ifPresent(agent ->
            xs2aRequest.setPartyAgent(mapAgentDescription(agent))
        );

        return xs2aRequest;
    }

    /**
     * After mapping callback to handle Optional fields for bulk VOP requests.
     * <p>Performance optimization: Delegates list mapping to extracted method.</p>
     *
     * @param request the bulk VOP API request with Optional fields
     * @param bulkVop the target domain object to populate
     */
    @AfterMapping
    default void mapBulkOptionalFields(BulkVerificationOfPartyRequest request,
                                       @MappingTarget BulkVop bulkVop) {
        // Map the list of party verification requests using extracted method
        bulkVop.setPartyVerificationRequests(
            mapPartyVerificationRequestList(request.getPartyVerificationRequests())
        );

        // Handle Optional<AgentDescription> requestingAgent
        request.getRequestingAgent()
            .map(this::mapAgentDescription)
            .ifPresent(bulkVop::setRequestingAgent);
    }

    /**
     * Maps a list of {@link de.adorsys.psd2.model.PartyVerificationRequest} objects.
     * <p>Extracted method to improve code reusability and reduce duplication.</p>
     * <p>Performance: Returns empty list for null input without stream overhead.</p>
     *
     * @param apiRequests the API party verification request list
     * @return mapped list, or empty list if input is {@code null} or empty
     */
    default List<de.adorsys.psd2.xs2a.core.vop.PartyVerificationRequest> mapPartyVerificationRequestList(
            List<de.adorsys.psd2.model.PartyVerificationRequest> apiRequests) {
        if (apiRequests == null || apiRequests.isEmpty()) {
            return List.of();
        }
        return apiRequests.stream()
            .map(this::mapPartyVerificationRequest)
            .toList();
    }

    /**
     * Maps PartyDescription4 from API to internal model.
     * Handles Optional name and PartyIdentification fields.
     *
     * @param apiParty the API party description
     * @return the internal party description
     */
    default de.adorsys.psd2.xs2a.core.vop.PartyDescription4 mapPartyDescription(PartyDescription4 apiParty) {
        if (apiParty == null) {
            return null;
        }

        de.adorsys.psd2.xs2a.core.vop.PartyDescription4 xs2aParty =
            new de.adorsys.psd2.xs2a.core.vop.PartyDescription4();

        // Map Optional<String> name
        apiParty.getName().ifPresent(xs2aParty::setName);

        // Map Optional<PartyIdentification> identification
        apiParty.getIdentification().ifPresent(identification ->
            xs2aParty.setIdentification(mapPartyIdentification(identification))
        );

        return xs2aParty;
    }

    /**
     * Maps PartyIdentification from API to internal model.
     * Handles Optional organisationId and privateId fields.
     *
     * @param apiIdentification the API party identification
     * @return the internal party identification
     */
    default de.adorsys.psd2.xs2a.core.vop.PartyIdentification mapPartyIdentification(
            PartyIdentification apiIdentification) {
        if (apiIdentification == null) {
            return null;
        }

        de.adorsys.psd2.xs2a.core.vop.PartyIdentification xs2aIdentification =
            new de.adorsys.psd2.xs2a.core.vop.PartyIdentification();

        // Map Optional<OrganisationIdentification> organisationId
        apiIdentification.getOrganisationId().ifPresent(orgId ->
            xs2aIdentification.setOrganisationId(mapOrganisationIdentification(orgId))
        );

        // Map Optional<PrivateIdentification> privateId
        apiIdentification.getPrivateId().ifPresent(privateId ->
            xs2aIdentification.setPrivateId(mapPrivateIdentification(privateId))
        );

        return xs2aIdentification;
    }

    /**
     * Maps {@link OrganisationIdentification} from API to internal model.
     * <p>Handles Optional fields anyBIC, lei, and the list of other identifications.</p>
     * <p>Performance optimization: Extracted list mapping to separate method for reusability.</p>
     *
     * @param apiOrgId the API organisation identification
     * @return the internal organisation identification, or {@code null} if input is {@code null}
     */
    default de.adorsys.psd2.xs2a.core.vop.OrganisationIdentification mapOrganisationIdentification(
            OrganisationIdentification apiOrgId) {
        if (apiOrgId == null) {
            return null;
        }

        de.adorsys.psd2.xs2a.core.vop.OrganisationIdentification xs2aOrgId =
            new de.adorsys.psd2.xs2a.core.vop.OrganisationIdentification();

        // Map Optional fields using method references
        apiOrgId.getAnyBIC().ifPresent(xs2aOrgId::setAnyBIC);
        apiOrgId.getLei().ifPresent(xs2aOrgId::setLei);

        // Map list using extracted method
        xs2aOrgId.setOthers(mapGenericOrganisationIdentificationList(apiOrgId.getOthers()));

        return xs2aOrgId;
    }

    /**
     * Maps a list of {@link GenericOrganisationIdentification} objects.
     * <p>Extracted method to improve code reusability and reduce duplication.</p>
     * <p>Performance: Returns empty list for null input without stream overhead.</p>
     *
     * @param apiGenericOrgIds the API generic organisation identification list
     * @return mapped list, or empty list if input is {@code null} or empty
     */
    default List<de.adorsys.psd2.xs2a.core.vop.GenericOrganisationIdentification> mapGenericOrganisationIdentificationList(
            List<GenericOrganisationIdentification> apiGenericOrgIds) {
        if (apiGenericOrgIds == null || apiGenericOrgIds.isEmpty()) {
            return List.of();
        }
        return apiGenericOrgIds.stream()
            .map(this::mapGenericOrganisationIdentification)
            .toList();
    }

    /**
     * Maps {@link GenericOrganisationIdentification} from API to internal model.
     * <p>Handles the identification field and Optional fields for scheme name and issuer.</p>
     * <p>Performance optimization: Uses Optional.map() to avoid lambda overhead.</p>
     *
     * @param apiGenericOrgId the API generic organisation identification
     * @return the internal generic organisation identification, or {@code null} if input is {@code null}
     */
    default de.adorsys.psd2.xs2a.core.vop.GenericOrganisationIdentification mapGenericOrganisationIdentification(
            GenericOrganisationIdentification apiGenericOrgId) {
        if (apiGenericOrgId == null) {
            return null;
        }

        de.adorsys.psd2.xs2a.core.vop.GenericOrganisationIdentification xs2aGenericOrgId =
            new de.adorsys.psd2.xs2a.core.vop.GenericOrganisationIdentification();

        // Map required field
        xs2aGenericOrgId.setIdentification(apiGenericOrgId.getIdentification());

        // Map Optional fields with optimized enum handling
        apiGenericOrgId.getSchemeNameCode()
            .map(OrganisationIdentificationCode::getValue)
            .ifPresent(xs2aGenericOrgId::setSchemeNameCode);

        apiGenericOrgId.getSchemeNameProprietary().ifPresent(xs2aGenericOrgId::setSchemeNameProprietary);
        apiGenericOrgId.getIssuer().ifPresent(xs2aGenericOrgId::setIssuer);

        return xs2aGenericOrgId;
    }

    /**
     * Maps {@link PrivateIdentification} from API to internal model.
     * <p>Handles Optional fields for birth information and the list of other person identifications.</p>
     * <p>Performance optimization: Uses method references and minimizes intermediate object creation.</p>
     *
     * @param apiPrivateId the API private identification
     * @return the internal private identification, or {@code null} if input is {@code null}
     */
    default de.adorsys.psd2.xs2a.core.vop.PrivateIdentification mapPrivateIdentification(
            PrivateIdentification apiPrivateId) {
        if (apiPrivateId == null) {
            return null;
        }

        de.adorsys.psd2.xs2a.core.vop.PrivateIdentification xs2aPrivateId =
            new de.adorsys.psd2.xs2a.core.vop.PrivateIdentification();

        // Map Optional birth-related fields using method references
        apiPrivateId.getBirthDate().ifPresent(xs2aPrivateId::setBirthDate);
        apiPrivateId.getProvinceOfBirth().ifPresent(xs2aPrivateId::setProvinceOfBirth);
        apiPrivateId.getCityOfBirth().ifPresent(xs2aPrivateId::setCityOfBirth);
        apiPrivateId.getCountryOfBirth().ifPresent(xs2aPrivateId::setCountryOfBirth);

        // Map List<PersonIdentification> - avoid empty check, toList() handles empty streams efficiently
        xs2aPrivateId.setOthers(mapPersonIdentificationList(apiPrivateId.getOthers()));

        return xs2aPrivateId;
    }

    /**
     * Maps a list of {@link PersonIdentification} objects.
     * <p>Extracted method to improve readability and reusability.</p>
     * <p>Performance: Returns empty list for null input without stream creation overhead.</p>
     *
     * @param apiPersonIds the API person identification list
     * @return mapped list, or empty list if input is {@code null} or empty
     */
    default List<de.adorsys.psd2.xs2a.core.vop.PersonIdentification> mapPersonIdentificationList(
            List<PersonIdentification> apiPersonIds) {
        if (apiPersonIds == null || apiPersonIds.isEmpty()) {
            return List.of(); // Immutable empty list - better performance than new ArrayList<>()
        }
        return apiPersonIds.stream()
            .map(this::mapPersonIdentification)
            .toList(); // Java 16+ - returns unmodifiable list
    }

    /**
     * Maps {@link PersonIdentification} from API to internal model.
     * <p>Handles the identification field and Optional fields for scheme name and issuer.</p>
     * <p>Performance optimization: Uses method references and minimizes lambda overhead.</p>
     *
     * @param apiPersonId the API person identification
     * @return the internal person identification, or {@code null} if input is {@code null}
     */
    default de.adorsys.psd2.xs2a.core.vop.PersonIdentification mapPersonIdentification(
            PersonIdentification apiPersonId) {
        if (apiPersonId == null) {
            return null;
        }

        de.adorsys.psd2.xs2a.core.vop.PersonIdentification xs2aPersonId =
            new de.adorsys.psd2.xs2a.core.vop.PersonIdentification();

        // Map required field
        xs2aPersonId.setIdentification(apiPersonId.getIdentification());

        // Map Optional fields with optimized handling
        apiPersonId.getSchemeNameCode()
            .map(PersonIdentificationCode::getValue)
            .ifPresent(xs2aPersonId::setSchemeNameCode);

        apiPersonId.getSchemeNameProprietary().ifPresent(xs2aPersonId::setSchemeNameProprietary);
        apiPersonId.getIssuer().ifPresent(xs2aPersonId::setIssuer);

        return xs2aPersonId;
    }

    /**
     * Maps AgentDescription from API to internal model.
     * Handles all nested fields including FinancialInstitutionIdentification.
     *
     * @param apiAgent the API agent description
     * @return the internal agent description
     */
    default de.adorsys.psd2.xs2a.core.vop.AgentDescription mapAgentDescription(AgentDescription apiAgent) {
        if (apiAgent == null) {
            return null;
        }

        de.adorsys.psd2.xs2a.core.vop.AgentDescription xs2aAgent =
            new de.adorsys.psd2.xs2a.core.vop.AgentDescription();

        // Map FinancialInstitutionIdentification (required field)
            xs2aAgent.setFinancialInstitutionId(
                mapFinancialInstitutionIdentification(apiAgent.getFinancialInstitutionId())
            );

        // Map Optional<String> branchIdentificationId
        apiAgent.getBranchIdentificationId().ifPresent(xs2aAgent::setBranchIdentificationId);

        // Map Optional<String> branchIdentificationName
        apiAgent.getBranchIdentificationName().ifPresent(xs2aAgent::setBranchIdentificationName);

        return xs2aAgent;
    }

    /**
     * Maps FinancialInstitutionIdentification from API to internal model.
     * Handles all nested Optional fields including clearingSystemMemberId,
     * postalAddress, and other.
     *
     * @param apiFinInst the API financial institution identification
     * @return the internal financial institution identification
     */
    default de.adorsys.psd2.xs2a.core.vop.FinancialInstitutionIdentification mapFinancialInstitutionIdentification(
            FinancialInstitutionIdentification apiFinInst) {
        if (apiFinInst == null) {
            return null;
        }

        de.adorsys.psd2.xs2a.core.vop.FinancialInstitutionIdentification xs2aFinInst =
            new de.adorsys.psd2.xs2a.core.vop.FinancialInstitutionIdentification();

        // Map Optional<String> bicfi
        apiFinInst.getBicfi().ifPresent(xs2aFinInst::setBicfi);

        // Map Optional<String> name
        apiFinInst.getName().ifPresent(xs2aFinInst::setName);

        // Map Optional<ClearingSystemMemberIdentification> clearingSystemMemberId
        apiFinInst.getClearingSystemMemberId().ifPresent(clearingSystem ->
            xs2aFinInst.setClearingSystemMemberId(mapClearingSystemMemberIdentification(clearingSystem))
        );

        // Map Optional<PostalAddress> postalAddress
        apiFinInst.getPostalAddress().ifPresent(postalAddress ->
            xs2aFinInst.setPostalAddress(mapPostalAddress(postalAddress))
        );

        // Map Optional<GenericFinancialInstitutionIdentification> other
        apiFinInst.getOther().ifPresent(other ->
            xs2aFinInst.setOther(mapGenericFinancialInstitutionIdentification(other))
        );

        return xs2aFinInst;
    }

    /**
     * Maps ClearingSystemMemberIdentification from API to internal model.
     * Handles Optional memberId and clearingSystemIdentification fields.
     *
     * @param apiClearingSystem the API clearing system member identification
     * @return the internal clearing system member identification
     */
    default de.adorsys.psd2.xs2a.core.vop.ClearingSystemMemberIdentification mapClearingSystemMemberIdentification(
            ClearingSystemMemberIdentification apiClearingSystem) {
        if (apiClearingSystem == null) {
            return null;
        }

        de.adorsys.psd2.xs2a.core.vop.ClearingSystemMemberIdentification xs2aClearingSystem =
            new de.adorsys.psd2.xs2a.core.vop.ClearingSystemMemberIdentification();

        // Map Optional<String> memberId
        apiClearingSystem.getMemberId().ifPresent(xs2aClearingSystem::setMemberId);

        // Map Optional<ClearingSystemIdentificationCode> clearingSystemIdentificationCode
        apiClearingSystem.getClearingSystemIdentificationCode().ifPresent(code ->
            xs2aClearingSystem.setClearingSystemIdentificationCode(code.getValue())
        );

        // Map Optional<String> clearingSystemIdentificationProprietary
        apiClearingSystem.getClearingSystemIdentificationProprietary().ifPresent(
            xs2aClearingSystem::setClearingSystemIdentificationProprietary
        );

        return xs2aClearingSystem;
    }

    /**
     * Maps PostalAddress from API to internal model.
     * Handles all address fields as plain Strings (xs2a-server-api model).
     *
     * @param apiAddress the API postal address
     * @return the internal postal address
     */
    default de.adorsys.psd2.xs2a.core.vop.PostalAddress mapPostalAddress(PostalAddress apiAddress) {
        if (apiAddress == null) {
            return null;
        }

        de.adorsys.psd2.xs2a.core.vop.PostalAddress xs2aAddress =
            new de.adorsys.psd2.xs2a.core.vop.PostalAddress();

        // Map String fields directly (xs2a-server-api uses String, not Optional<String>)
        xs2aAddress.setStreetName(apiAddress.getStreetName());
        xs2aAddress.setBuildingNumber(apiAddress.getBuildingNumber());
        xs2aAddress.setPostCode(apiAddress.getPostCode());
        xs2aAddress.setTownName(apiAddress.getTownName());
        xs2aAddress.setCountry(apiAddress.getCountry());

        // Map List<String> addressLines (direct assignment)
        xs2aAddress.setAddressLines(apiAddress.getAddressLines());

        return xs2aAddress;
    }

    /**
     * Maps GenericFinancialInstitutionIdentification from API to internal model.
     * Handles identification, schemeName, and issuer fields.
     *
     * @param apiOther the API generic financial institution identification
     * @return the internal generic financial institution identification
     */
    default de.adorsys.psd2.xs2a.core.vop.GenericFinancialInstitutionIdentification mapGenericFinancialInstitutionIdentification(
            GenericFinancialInstitutionIdentification apiOther) {
        if (apiOther == null) {
            return null;
        }

        de.adorsys.psd2.xs2a.core.vop.GenericFinancialInstitutionIdentification xs2aOther =
            new de.adorsys.psd2.xs2a.core.vop.GenericFinancialInstitutionIdentification();

        // Map String identification (required field)
        xs2aOther.setIdentification(apiOther.getIdentification());

        // Map Optional<String> schemeNameCode
        apiOther.getSchemeNameCode().ifPresent(xs2aOther::setSchemeNameCode);

        // Map Optional<String> schemeNameProprietary
        apiOther.getSchemeNameProprietary().ifPresent(xs2aOther::setSchemeNameProprietary);

        // Map Optional<String> issuer
        apiOther.getIssuer().ifPresent(xs2aOther::setIssuer);

        return xs2aOther;
    }
}
