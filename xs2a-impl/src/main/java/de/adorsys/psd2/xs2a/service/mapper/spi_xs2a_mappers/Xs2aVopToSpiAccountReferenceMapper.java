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

import de.adorsys.psd2.xs2a.core.vop.AccountReference;
import de.adorsys.psd2.xs2a.spi.domain.vop.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting VOP API AccountReference to SPI SpiAccountReference.
 * Handles all nested complex types including GenericAccountIdentification, ProxyAccountIdentification,
 * CashAccountType, PartyDescription2, and FinancialInstitutionIdentification.
 */
@Component
@RequiredArgsConstructor
public class Xs2aVopToSpiAccountReferenceMapper {

    private final Xs2aToSpiFinancialInstitutionIdentificationMapper financialInstitutionIdentificationMapper;

    /**
     * Maps xs2a-core VOP AccountReference to SPI SpiAccountReference.
     *
     * @param source the xs2a-core VOP AccountReference (from de.adorsys.psd2.xs2a.core.vop package) to map
     * @return the mapped SpiAccountReference, or null if source is null
     */
    public SpiAccountReference mapToSpiAccountReference(AccountReference source) {
        if (source == null) {
            return null;
        }

        SpiAccountReference target = new SpiAccountReference();

        // Map simple String fields (source has plain fields, not Optional)
        target.setIban(source.getIban());
        target.setBban(source.getBban());
        target.setPan(source.getPan());
        target.setMaskedPan(source.getMaskedPan());
        target.setMsisdn(source.getMsisdn());
        target.setTypeProprietary(source.getTypeProprietary());
        target.setCurrency(source.getCurrency());
        target.setName(source.getName());

        // Map complex nested types
        target.setOther(mapToSpiGenericAccountIdentification(source.getOther()));
        target.setTypeCode(mapToSpiCashAccountType(source.getTypeCode()));
        target.setProxy(mapToSpiProxyAccountIdentification(source.getProxy()));
        target.setOwner(mapToSpiPartyDescription2(source.getOwner()));
        target.setServicer(mapToSpiFinancialInstitutionIdentification(source.getServicer()));

        return target;
    }

    /**
     * Maps xs2a-core GenericAccountIdentification to SpiGenericAccountIdentification.
     *
     * @param source the GenericAccountIdentification to map
     * @return the mapped SpiGenericAccountIdentification, or null if source is null
     */
    private SpiGenericAccountIdentification mapToSpiGenericAccountIdentification(de.adorsys.psd2.xs2a.core.vop.GenericAccountIdentification source) {
        if (source == null) {
            return null;
        }

        SpiGenericAccountIdentification target = new SpiGenericAccountIdentification();
        target.setIdentification(source.getIdentification());
        target.setSchemeNameCode(mapToSpiAccountIdentificationCode(source.getSchemeNameCode()));
        target.setSchemeNameProprietary(source.getSchemeNameProprietary());
        target.setIssuer(source.getIssuer());

        return target;
    }

    /**
     * Maps String schemeNameCode to SpiAccountIdentificationCode enum.
     *
     * @param code the String code value
     * @return the corresponding SpiAccountIdentificationCode enum value, or null if invalid
     */
    private SpiAccountIdentificationCode mapToSpiAccountIdentificationCode(String code) {
        if (code == null) {
            return null;
        }

        try {
            return SpiAccountIdentificationCode.valueOf(code);
        } catch (IllegalArgumentException e) {
            // Return null for invalid/unknown codes
            return null;
        }
    }

    /**
     * Maps xs2a-core CashAccountType enum to SpiCashAccountType enum.
     *
     * @param typeCode the CashAccountType enum value
     * @return the corresponding SpiCashAccountType enum value, or null if invalid
     */
    private SpiCashAccountType mapToSpiCashAccountType(de.adorsys.psd2.xs2a.core.vop.CashAccountType typeCode) {
        if (typeCode == null) {
            return null;
        }

        try {
            return SpiCashAccountType.valueOf(typeCode.name());
        } catch (IllegalArgumentException e) {
            // Return null for invalid/unknown type codes
            return null;
        }
    }

    /**
     * Maps xs2a-core ProxyAccountIdentification to SpiProxyAccountIdentification.
     *
     * @param source the ProxyAccountIdentification to map
     * @return the mapped SpiProxyAccountIdentification, or null if source is null
     */
    private SpiProxyAccountIdentification mapToSpiProxyAccountIdentification(de.adorsys.psd2.xs2a.core.vop.ProxyAccountIdentification source) {
        if (source == null) {
            return null;
        }

        SpiProxyAccountIdentification target = new SpiProxyAccountIdentification();
        target.setTypeCode(mapToSpiProxyAccountTypeCode(source.getTypeCode()));
        target.setTypeProprietary(source.getTypeProprietary());
        target.setIdentification(source.getIdentification());

        return target;
    }

    /**
     * Maps String typeCode to SpiProxyAccountTypeCode enum.
     *
     * @param typeCode the String proxy type code value
     * @return the corresponding SpiProxyAccountTypeCode enum value, or null if invalid
     */
    private SpiProxyAccountTypeCode mapToSpiProxyAccountTypeCode(String typeCode) {
        if (typeCode == null) {
            return null;
        }

        try {
            return SpiProxyAccountTypeCode.valueOf(typeCode);
        } catch (IllegalArgumentException e) {
            // Return null for invalid/unknown proxy type codes
            return null;
        }
    }

    /**
     * Maps xs2a-core PartyDescription2 to SpiPartyDescription2.
     *
     * @param source the PartyDescription2 to map
     * @return the mapped SpiPartyDescription2, or null if source is null
     */
    private SpiPartyDescription2 mapToSpiPartyDescription2(de.adorsys.psd2.xs2a.core.vop.PartyDescription2 source) {
        if (source == null) {
            return null;
        }

        SpiPartyDescription2 target = new SpiPartyDescription2();
        target.setName(source.getName());
        target.setPostaladdress(mapToSpiPostalAddress(source.getPostalAddress()));

        return target;
    }

    /**
     * Maps xs2a-core PostalAddress to SPI SpiPostalAddress.
     *
     * @param source the xs2a-core PostalAddress to map
     * @return the mapped SpiPostalAddress, or null if source is null
     */
    private SpiPostalAddress mapToSpiPostalAddress(de.adorsys.psd2.xs2a.core.vop.PostalAddress source) {
        if (source == null) {
            return null;
        }

        SpiPostalAddress target = new SpiPostalAddress();
        target.setStreetName(source.getStreetName());
        target.setBuildingNumber(source.getBuildingNumber());
        target.setPostCode(source.getPostCode());
        target.setTownName(source.getTownName());
        target.setCountry(source.getCountry());
        target.setDepartment(source.getDepartment());
        target.setSubDepartment(source.getSubDepartment());
        target.setBuildingName(source.getBuildingName());
        target.setFloor(source.getFloor());
        target.setPostBox(source.getPostBox());
        target.setRoom(source.getRoom());
        target.setTownLocationName(source.getTownLocationName());
        target.setDistrictName(source.getDistrictName());
        target.setCountrySubDivision(source.getCountrySubDivision());
        target.setAddressLines(source.getAddressLines());

        return target;
    }

    /**
     * Maps xs2a-core FinancialInstitutionIdentification to SPI SpiFinancialInstitutionIdentification.
     *
     * @param source the xs2a-core FinancialInstitutionIdentification to map
     * @return the mapped SpiFinancialInstitutionIdentification, or null if source is null
     */
    private SpiFinancialInstitutionIdentification mapToSpiFinancialInstitutionIdentification(de.adorsys.psd2.xs2a.core.vop.FinancialInstitutionIdentification source) {
        if (source == null) {
            return null;
        }

        return financialInstitutionIdentificationMapper.mapToSpiFinancialInstitutionIdentification(source);
    }
}
