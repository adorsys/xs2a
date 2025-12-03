package de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers;

import de.adorsys.psd2.xs2a.core.vop.AgentDescription;
import de.adorsys.psd2.xs2a.core.vop.PartyDescription4;
import de.adorsys.psd2.xs2a.domain.vop.SingleVop;
import de.adorsys.psd2.xs2a.spi.domain.vop.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting XS2A SingleVop domain objects to SPI SpiSingleVop objects.
 * Follows the pattern established in Xs2aToSpiSinglePaymentMapper.
 */
@Component
@RequiredArgsConstructor
public class Xs2aToSpiSingleVopMapper {

    private final Xs2aToSpiFinancialInstitutionIdentificationMapper financialInstitutionIdentificationMapper;
    private final Xs2aToSpiPartyIdentificationMapper partyIdentificationMapper;
    private final Xs2aVopToSpiAccountReferenceMapper vopToSpiAccountReferenceMapper;

    /**
     * Maps a SingleVop domain object to a SpiSingleVop SPI object.
     *
     * @param singleVop                  the XS2A SingleVop domain object to map
     * @param partyVerificationEndpoint  the party verification endpoint URL
     * @return the mapped SpiSingleVop object
     */
    public SpiSingleVop mapToSpiSingleVop(SingleVop singleVop, String partyVerificationEndpoint) {
        if (singleVop == null) {
            return null;
        }

        SpiSingleVop spiSingleVop = new SpiSingleVop();

        // Map common VOP fields from CommonVop
        spiSingleVop.setVopId(singleVop.getVopId());
        spiSingleVop.setContentType(singleVop.getContentType());
        spiSingleVop.setInstanceId(singleVop.getInstanceId());
        spiSingleVop.setCreationTimestamp(singleVop.getCreationTimestamp());

        // Set the party verification endpoint
        spiSingleVop.setPartyVerificationEndpoint(partyVerificationEndpoint);

        // Map SingleVop specific fields
        spiSingleVop.setPartyAccount(vopToSpiAccountReferenceMapper.mapToSpiAccountReference(singleVop.getPartyAccount()));
        spiSingleVop.setPartyAgent(mapToSpiAgentDescription(singleVop.getPartyAgent()));
        spiSingleVop.setParty(mapToSpiPartyDescription4(singleVop.getParty()));
        spiSingleVop.setUnstructuredRemittanceInformation(singleVop.getUnstructuredRemittanceInformation());
        spiSingleVop.setRequestingAgent(mapToSpiAgentDescription(singleVop.getRequestingAgent()));

        return spiSingleVop;
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
