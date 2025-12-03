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

package de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers;

import de.adorsys.psd2.xs2a.core.vop.AgentDescription;
import de.adorsys.psd2.xs2a.core.vop.PartyDescription4;
import de.adorsys.psd2.xs2a.core.vop.PartyVerificationRequest;
import de.adorsys.psd2.xs2a.domain.vop.BulkVop;
import de.adorsys.psd2.xs2a.spi.domain.vop.*;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Mapper for converting XS2A BulkVop domain objects to SPI SpiBulkVop objects.
 * Follows the pattern established in Xs2aToSpiSingleVopMapper.
 */
@Component
@RequiredArgsConstructor
public class Xs2aToSpiBulkVopMapper {

    private final Xs2aToSpiFinancialInstitutionIdentificationMapper financialInstitutionIdentificationMapper;
    private final Xs2aToSpiPartyIdentificationMapper partyIdentificationMapper;
    private final Xs2aVopToSpiAccountReferenceMapper vopToSpiAccountReferenceMapper;

    /**
     * Maps a BulkVop domain object to a SpiBulkVop SPI object.
     *
     * @param bulkVop                       the XS2A BulkVop domain object to map
     * @param bulkPartyVerificationEndpoint the bulk party verification endpoint URL
     * @return the mapped SpiBulkVop object
     */
    public SpiBulkVop mapToSpiBulkVop(BulkVop bulkVop, String bulkPartyVerificationEndpoint) {
        if (bulkVop == null) {
            return null;
        }

        SpiBulkVop spiBulkVop = new SpiBulkVop();

        // Map common VOP fields from CommonVop
        spiBulkVop.setVopId(bulkVop.getVopId());
        spiBulkVop.setContentType(bulkVop.getContentType());
        spiBulkVop.setInstanceId(bulkVop.getInstanceId());
        spiBulkVop.setCreationTimestamp(bulkVop.getCreationTimestamp());

        // Set the bulk party verification endpoint
        spiBulkVop.setBulkpartyVerificationEndpoint(bulkPartyVerificationEndpoint);

        // Map BulkVop specific fields
        spiBulkVop.setPartyVerificationRequests(
            mapToSpiPartyVerificationRequests(bulkVop.getPartyVerificationRequests())
        );
        spiBulkVop.setRequestingAgent(mapToSpiAgentDescription(bulkVop.getRequestingAgent()));

        return spiBulkVop;
    }

    /**
     * Maps a list of PartyVerificationRequest to a list of SpiPartyVerificationRequest.
     *
     * @param requests the XS2A PartyVerificationRequest list to map
     * @return the mapped list of SpiPartyVerificationRequest
     */
    private List<SpiPartyVerificationRequest> mapToSpiPartyVerificationRequests(List<PartyVerificationRequest> requests) {
        if (requests == null) {
            return Collections.emptyList();
        }

        return requests.stream()
            .map(this::mapToSpiPartyVerificationRequest)
            .toList();
    }

    /**
     * Maps a PartyVerificationRequest to SpiPartyVerificationRequest.
     *
     * @param request the XS2A PartyVerificationRequest to map
     * @return the mapped SpiPartyVerificationRequest
     */
    private SpiPartyVerificationRequest mapToSpiPartyVerificationRequest(PartyVerificationRequest request) {
        if (request == null) {
            return null;
        }

        SpiPartyVerificationRequest spiRequest = new SpiPartyVerificationRequest();
        spiRequest.setUetr(request.getUetr());
        spiRequest.setPartyAccount(vopToSpiAccountReferenceMapper.mapToSpiAccountReference(request.getPartyAccount()));
        spiRequest.setPartyAgent(mapToSpiAgentDescription(request.getPartyAgent()));
        spiRequest.setParty(mapToSpiPartyDescription4(request.getParty()));

        return spiRequest;
    }

    /**
     * Maps AgentDescription to SpiAgentDescription.
     *
     * @param agentDescription the XS2A AgentDescription to map
     * @return the mapped SpiAgentDescription
     */
    private SpiAgentDescription mapToSpiAgentDescription(AgentDescription agentDescription) {
        if (agentDescription == null) {
            return null;
        }

        SpiAgentDescription spiAgentDescription = new SpiAgentDescription();
        spiAgentDescription.setFinancialInstitutionId(
            financialInstitutionIdentificationMapper.mapToSpiFinancialInstitutionIdentification(agentDescription.getFinancialInstitutionId())
        );
        spiAgentDescription.setBranchIdentificationId(agentDescription.getBranchIdentificationId());
        spiAgentDescription.setBranchIdentificationName(agentDescription.getBranchIdentificationName());

        return spiAgentDescription;
    }

    /**
     * Maps PartyDescription4 to SpiPartyDescription4.
     *
     * @param partyDescription the XS2A PartyDescription4 to map
     * @return the mapped SpiPartyDescription4
     */
    private SpiPartyDescription4 mapToSpiPartyDescription4(PartyDescription4 partyDescription) {
        if (partyDescription == null) {
            return null;
        }

        SpiPartyDescription4 spiPartyDescription = new SpiPartyDescription4();
        spiPartyDescription.setName(partyDescription.getName());
        spiPartyDescription.setIdentification(
            partyIdentificationMapper.mapToSpiPartyIdentification(partyDescription.getIdentification())
        );

        return spiPartyDescription;
    }
}
