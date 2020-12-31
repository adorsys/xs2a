package de.adorsys.psd2.validator.certificate.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.net.URLCodec;

@Slf4j
public class URLDecodingUtil {
    private URLDecodingUtil() {}

    public static byte[] decode(byte[] encodedCert) {
        try {
            return URLCodec.decodeUrl(encodedCert);
        } catch (DecoderException e) {
            log.debug("Error URL-decoding the data");
        }
        return new byte[0];
    }
}
