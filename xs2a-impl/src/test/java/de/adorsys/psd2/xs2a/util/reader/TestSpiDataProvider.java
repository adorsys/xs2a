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

package de.adorsys.psd2.xs2a.util.reader;

import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;

import java.util.UUID;

public class TestSpiDataProvider {

    private static final UUID X_REQUEST_ID = UUID.randomUUID();
    private static final UUID INTERNAL_REQUEST_ID = UUID.randomUUID();
    private static final String AUTHORISATION = "Bearer 1111111";
    private static final String PSU_ID = "psuId";
    private static final String PSU_ID_TYPE = "psuIdType";
    private static final String PSU_CORPORATE_ID = "psuCorporateId";
    private static final String PSU_CORPORATE_ID_TYPE = "psuCorporateIdType";
    private static final String PSU_IP_ADDRESS = "psuIpAddress";
    private static final String PSU_IP_PORT = "psuIpPort";
    private static final String PSU_USER_AGENT = "psuUserAgent";
    private static final String PSU_GEO_LOCATION = "psuGeoLocation";
    private static final String PSU_ACCEPT = "psuAccept";
    private static final String PSU_ACCEPT_CHARSET = "psuAcceptCharset";
    private static final String PSU_ACCEPT_ENCODING = "psuAcceptEncoding";
    private static final String PSU_ACCEPT_LANGUAGE = "psuAcceptLanguage";
    private static final String PSU_HTTP_METHOD = "psuHttpMethod";
    private static final UUID PSU_DEVICE_ID = UUID.randomUUID();
    private static final String TPP_BRAND_LOGGING_INFORMATION = "tppBrandLoggingInformation";
    private static final Boolean TPP_REJECTION_NO_FUNDS_PREFFERED = true;
    private static final Boolean TPP_DECOUPLED_PREFERRED_HEADER = true;
    private static final Boolean TPP_REDIRECT_PREFERRED_HEADER = true;

    public static SpiContextData getSpiContextData() {
        return new SpiContextData(
            SpiPsuData.builder()
                .psuId(PSU_ID)
                .psuIdType(PSU_ID_TYPE)
                .psuCorporateId(PSU_CORPORATE_ID)
                .psuCorporateIdType(PSU_CORPORATE_ID_TYPE)
                .psuIpAddress(PSU_IP_ADDRESS)
                .psuIpPort(PSU_IP_PORT)
                .psuUserAgent(PSU_USER_AGENT)
                .psuGeoLocation(PSU_GEO_LOCATION)
                .psuAccept(PSU_ACCEPT)
                .psuAcceptCharset(PSU_ACCEPT_CHARSET)
                .psuAcceptEncoding(PSU_ACCEPT_ENCODING)
                .psuAcceptLanguage(PSU_ACCEPT_LANGUAGE)
                .psuHttpMethod(PSU_HTTP_METHOD)
                .psuDeviceId(PSU_DEVICE_ID).build(),
            new TppInfo(),
            X_REQUEST_ID,
            INTERNAL_REQUEST_ID,
            AUTHORISATION,
            TPP_BRAND_LOGGING_INFORMATION,
            TPP_REJECTION_NO_FUNDS_PREFFERED,
            TPP_DECOUPLED_PREFERRED_HEADER,
            TPP_REDIRECT_PREFERRED_HEADER
        );
    }

    public static SpiContextData defaultSpiContextData() {
        return new SpiContextData(null, null, null, null, null, null, null, null, null);
    }

    public static SpiContextData buildWithPsuTppAuthToken(SpiPsuData psuData, TppInfo tppInfo, String oAuth2Token) {
        return new SpiContextData(psuData, tppInfo, X_REQUEST_ID, INTERNAL_REQUEST_ID, oAuth2Token, TPP_BRAND_LOGGING_INFORMATION, null, null, null);
    }
}
