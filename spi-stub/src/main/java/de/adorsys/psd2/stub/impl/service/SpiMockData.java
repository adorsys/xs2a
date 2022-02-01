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

package de.adorsys.psd2.stub.impl.service;

import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.spi.domain.common.SpiAuthenticationObject;
import de.adorsys.psd2.xs2a.spi.domain.common.SpiHrefType;
import de.adorsys.psd2.xs2a.spi.domain.common.SpiLinks;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SpiMockData {
    public static final SpiLinks SPI_LINKS = buildSpiLinks();
    public static final Set<TppMessageInformation> TPP_MESSAGES = buildTppMessages();
    public static final Set<TppMessageInformation> TPP_MESSAGES_START_AUTHORISATION = buildTppMessagesStartAuthorisation();
    public static final List<SpiAuthenticationObject> SCA_METHODS = buildScaMethods();
    public static final String PSU_MESSAGE = "mocked PSU message from spi stub";
    public static final String PSU_MESSAGE_START_AUTHORISATION = "Start authorisation mocked PSU message from spi stub";

    private static SpiLinks buildSpiLinks() {
        SpiLinks spiLinks = new SpiLinks();
        spiLinks.setAccount(new SpiHrefType("Mock spi account link from spi stub"));
        return spiLinks;
    }

    private static Set<TppMessageInformation> buildTppMessages() {
        HashSet<TppMessageInformation> tppInformationSet = new HashSet<>();
        tppInformationSet.add(TppMessageInformation.buildWithCustomWarning(MessageErrorCode.FORMAT_ERROR, "Mocked tpp message from spi stub"));
        return tppInformationSet;
    }

    private static Set<TppMessageInformation> buildTppMessagesStartAuthorisation() {
        HashSet<TppMessageInformation> tppInformationSet = new HashSet<>();
        tppInformationSet.add(TppMessageInformation.buildWithCustomWarning(MessageErrorCode.FORMAT_ERROR, "Start authorisation Mocked tpp message from spi stub"));
        return tppInformationSet;
    }

    private static List<SpiAuthenticationObject> buildScaMethods() {
        SpiAuthenticationObject psi = new SpiAuthenticationObject();
        psi.setAuthenticationType("Mocked Authentication type from spi stub");
        psi.setAuthenticationMethodId("Mocked Authentication id from spi stub");
        psi.setDecoupled(false);
        psi.setName("Mocked name from spi stub");
        psi.setAuthenticationVersion("Mocked Authentication version from spi stub");

        return Collections.singletonList(psi);
    }
}
