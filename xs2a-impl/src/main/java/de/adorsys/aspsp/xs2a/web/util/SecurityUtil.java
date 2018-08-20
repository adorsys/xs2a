package de.adorsys.aspsp.xs2a.web.util;

import de.adorsys.psd2.validator.certificate.util.TppCertificateData;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;
import java.util.Objects;

/**
 * Utility class to get TPP data from anywhere
 */
public class SecurityUtil {

    public static TppCertificateData getTppCertificateData() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (Objects.nonNull(authentication)) {
            Object credentials = authentication.getCredentials();
            if (credentials instanceof Map) {

                TppCertificateData tppCertificateData = new TppCertificateData();

                tppCertificateData.setPspAuthorityCountry(((Map<String, String>) credentials).get("authorityCountry"));
                tppCertificateData.setPspAuthorityId(((Map<String, String>) credentials).get("authorityId"));
                tppCertificateData.setPspAuthorityName(((Map<String, String>) credentials).get("authorityName"));
                tppCertificateData
                    .setPspAuthorizationNumber(((Map<String, String>) credentials).get("authorizationNumber"));
                tppCertificateData.setPspName(((Map<String, String>) credentials).get("name"));

                return tppCertificateData;

            }
        }
        return null;
    }

    public static String getTppAutorisationNumber() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (Objects.nonNull(authentication)) {
            return authentication.getName();
        }
        return null;
    }
}
