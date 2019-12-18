package de.adorsys.psd2.xs2a.core.psu;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

@Data
@NoArgsConstructor
public class AdditionalPsuIdData {
    private String psuIpPort;
    private String psuUserAgent;
    private String psuGeoLocation;
    private String psuAccept;
    private String psuAcceptCharset;
    private String psuAcceptEncoding;
    private String psuAcceptLanguage;
    private String psuHttpMethod;
    private UUID psuDeviceId;

    public AdditionalPsuIdData( String psuIpPort, String psuUserAgent, String psuGeoLocation, String psuAccept, String psuAcceptCharset,
                               String psuAcceptEncoding, String psuAcceptLanguage, String psuHttpMethod, UUID psuDeviceId) {
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

    @JsonIgnore
    public boolean isEmpty() {
        return Stream.of(psuIpPort, psuUserAgent, psuGeoLocation, psuAccept, psuAcceptCharset, psuAcceptEncoding, psuAcceptLanguage, psuHttpMethod, psuDeviceId).allMatch(Objects::isNull);
    }

    @JsonIgnore
    public boolean isNotEmpty() {
        return !isEmpty();
    }

}
