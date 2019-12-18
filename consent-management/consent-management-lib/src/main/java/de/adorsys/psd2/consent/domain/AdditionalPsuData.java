package de.adorsys.psd2.consent.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@NoArgsConstructor
@Data
public class AdditionalPsuData {
    @Id
    @Column(name = "id")
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

    public AdditionalPsuData(String psuIpPort, String psuUserAgent, String psuGeoLocation, String psuAccept, String psuAcceptCharset, String psuAcceptEncoding, String psuAcceptLanguage, String psuHttpMethod, String psuDeviceId) {
        this.psuIpPort = psuIpPort;
        this.psuUserAgent = psuUserAgent;
        this.psuGeoLocation = psuGeoLocation;
        this.psuAccept = psuAccept;
        this.psuAcceptCharset = psuAcceptCharset;
        this.psuAcceptEncoding = psuAcceptEncoding;
        this.psuAcceptLanguage = psuAcceptLanguage;
        this.psuHttpMethod = psuHttpMethod;
        this.psuDeviceId = psuDeviceId;
    }
}
