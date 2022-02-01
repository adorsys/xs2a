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

package de.adorsys.psd2.consent.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@NoArgsConstructor
@Data
public class AdditionalPsuData {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "additional_psu_data_generator")
    @SequenceGenerator(name = "additional_psu_data_generator", sequenceName = "additional_psu_data_id_seq", allocationSize = 1)
    private Long id;
    @Column
    private String psuIpPort;
    @Column
    private String psuUserAgent;
    @Column
    private String psuGeoLocation;
    @Column
    private String psuAccept;
    @Column
    private String psuAcceptCharset;
    @Column
    private String psuAcceptEncoding;
    @Column
    private String psuAcceptLanguage;
    @Column
    private String psuHttpMethod;
    @Column
    private String psuDeviceId;

    public AdditionalPsuData psuIpPort(String psuIpPort) {
        this.psuIpPort = psuIpPort;
        return this;
    }

    public AdditionalPsuData psuUserAgent(String psuUserAgent) {
        this.psuUserAgent = psuUserAgent;
        return this;
    }

    public AdditionalPsuData psuGeoLocation(String psuGeoLocation) {
        this.psuGeoLocation = psuGeoLocation;
        return this;
    }

    public AdditionalPsuData psuAccept(String psuAccept) {
        this.psuAccept = psuAccept;
        return this;
    }

    public AdditionalPsuData psuAcceptCharset(String psuAcceptCharset) {
        this.psuAcceptCharset = psuAcceptCharset;
        return this;
    }

    public AdditionalPsuData psuAcceptEncoding(String psuAcceptEncoding) {
        this.psuAcceptEncoding = psuAcceptEncoding;
        return this;
    }

    public AdditionalPsuData psuAcceptLanguage(String psuAcceptLanguage) {
        this.psuAcceptLanguage = psuAcceptLanguage;
        return this;
    }

    public AdditionalPsuData psuHttpMethod(String psuHttpMethod) {
        this.psuHttpMethod = psuHttpMethod;
        return this;
    }

    public AdditionalPsuData psuDeviceId(String psuDeviceId) {
        this.psuDeviceId = psuDeviceId;
        return this;
    }
}
