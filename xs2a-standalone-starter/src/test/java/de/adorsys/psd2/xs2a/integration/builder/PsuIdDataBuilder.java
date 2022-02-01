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

package de.adorsys.psd2.xs2a.integration.builder;

import de.adorsys.psd2.xs2a.core.psu.AdditionalPsuIdData;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;

import java.util.UUID;

public class PsuIdDataBuilder {
    private static final String PSU_ID = "PSU-123";
    private static final String PSU_ID_TYPE = "Some type";
    private static final String PSU_CORPORATE_ID = "Some corporate id";
    private static final String PSU_CORPORATE_ID_TYPE = "Some corporate id type";
    private static final String PSU_IP_ADDRESS = "1.1.1.1";
    private static final String PSU_IP_PORT = "1111";
    private static final String PSU_USER_AGENT = "Some user agent";
    private static final String PSU_GEO_LOCATION = "Some geo location";
    private static final String PSU_ACCEPT = "Some accept";
    private static final String PSU_ACCEPT_CHARSET = "Some accept-charset";
    private static final String PSU_ACCEPT_ENCODING = "Some accept-encoding";
    private static final String PSU_ACCEPT_LANGUAGE = "Some accept-language";
    private static final String PSU_HTTP_METHOD = "Some http method";
    private static final String PSU_DEVICE_ID = "d7d369a9-898d-4682-b586-0a63ffe43a2c";


    public static PsuIdData buildPsuIdData() {
        return new PsuIdData(PSU_ID, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_ID_TYPE, PSU_IP_ADDRESS, buildAdditionalPsuIdData());
    }

    public static PsuIdData buildPsuIdData(String psuId) {
        return new PsuIdData(psuId, null, null, null, null);
    }

    public static PsuIdData buildPsuIdDataWithIpAddress() {
        return new PsuIdData(null, null, null, null, PSU_IP_ADDRESS, buildEmptyAdditionalPsuIdData());
    }

    private static AdditionalPsuIdData buildAdditionalPsuIdData() {
        return new AdditionalPsuIdData(PSU_IP_PORT, PSU_USER_AGENT, PSU_GEO_LOCATION, PSU_ACCEPT, PSU_ACCEPT_CHARSET, PSU_ACCEPT_ENCODING, PSU_ACCEPT_LANGUAGE, PSU_HTTP_METHOD, UUID.fromString(PSU_DEVICE_ID));
    }

    private static AdditionalPsuIdData buildEmptyAdditionalPsuIdData() {
        return new AdditionalPsuIdData(null, null, null, null, null, null, null, null, null);
    }
}
