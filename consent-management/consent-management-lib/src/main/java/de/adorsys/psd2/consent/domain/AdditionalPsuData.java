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
