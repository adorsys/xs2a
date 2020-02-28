package de.adorsys.psd2.consent.service.mapper;

import de.adorsys.psd2.consent.domain.consent.ConsentTppInformationEntity;
import de.adorsys.psd2.xs2a.core.consent.ConsentTppInformation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring", uses = {TppInfoMapper.class})
public interface ConsentTppInformationMapper {

    @Mapping(target = "tppNotificationSupportedModes", source = "tppNotificationContentPreferred")
    ConsentTppInformation mapToConsentTppInformation(ConsentTppInformationEntity consentTppInformationEntity);

    @Mapping(target = "tppNotificationContentPreferred", source = "tppNotificationSupportedModes")
    ConsentTppInformationEntity mapToConsentTppInformationEntity(ConsentTppInformation consentTppInformation);
}
