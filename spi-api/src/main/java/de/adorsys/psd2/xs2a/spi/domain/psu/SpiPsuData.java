/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.psd2.xs2a.spi.domain.psu;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

/**
 * Contains data about PSU known in scope of the request
 */
@Builder
@Getter
public class SpiPsuData {
    private String psuId;
    private String psuIdType;
    private String psuCorporateId;
    private String psuCorporateIdType;
    private String psuIpAddress;
    private String psuIpPort;
    private String psuUserAgent;
    private String psuGeoLocation;
    private String psuAccept;
    private String psuAcceptCharset;
    private String psuAcceptEncoding;
    private String psuAcceptLanguage;
    private String psuHttpMethod;
    private UUID psuDeviceId;
}
