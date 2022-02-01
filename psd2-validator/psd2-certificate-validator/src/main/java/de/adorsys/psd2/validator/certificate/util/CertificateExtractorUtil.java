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

package de.adorsys.psd2.validator.certificate.util;

import com.nimbusds.jose.util.X509CertUtils;
import de.adorsys.psd2.validator.certificate.CertificateErrorMsgCode;
import de.adorsys.psd2.validator.common.PSD2QCStatement;
import de.adorsys.psd2.validator.common.PSD2QCType;
import de.adorsys.psd2.validator.common.RoleOfPSP;
import de.adorsys.psd2.validator.common.RolesOfPSP;
import lombok.extern.slf4j.Slf4j;
import no.difi.certvalidator.api.CertificateValidationException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import java.security.Principal;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class CertificateExtractorUtil {
    private static final String LDAP_COMMON_NAME = "CN";

    private CertificateExtractorUtil() {
    }

    public static TppCertificateData extract(String encodedCert) throws CertificateValidationException {
        X509Certificate cert = X509CertUtils.parse(encodedCert);
        if (cert == null) {
            byte[] encodedCertData = encodedCert.getBytes();
            cert = X509CertUtils.parse(URLDecodingUtil.decode(encodedCertData));
        }

        if (cert == null) {
            log.debug("Error reading certificate ");
            throw new CertificateValidationException(CertificateErrorMsgCode.CERTIFICATE_INVALID.toString());
        }

        List<String> roles = new ArrayList<>();

        TppCertificateData tppCertData = new TppCertificateData();

        PSD2QCType psd2qcType = PSD2QCStatement.psd2QCType(cert);
        RolesOfPSP rolesOfPSP = psd2qcType.getRolesOfPSP();
        RoleOfPSP[] roles2 = rolesOfPSP.getRoles();
        for (RoleOfPSP roleOfPSP : roles2) {
            roles.add(roleOfPSP.getNormalizedRoleName());
        }
        tppCertData.setPspRoles(roles);

        tppCertData.setPspAuthorityName(psd2qcType.getnCAName().getString());
        tppCertData.setPspAuthorityId(psd2qcType.getnCAId().getString());
        tppCertData.setIssuerCN(extractIssuerCNFromIssuerDN(cert.getIssuerDN()));
        tppCertData.setNotAfter(cert.getNotAfter());

        try {
            X500Name x500name = new JcaX509CertificateHolder(cert).getSubject();

            tppCertData.setPspAuthorisationNumber(getValueFromX500Name(x500name, BCStyle.ORGANIZATION_IDENTIFIER));
            tppCertData.setOrganisation(getValueFromX500Name(x500name, BCStyle.O));
            tppCertData.setOrganisationUnit(getValueFromX500Name(x500name, BCStyle.OU));
            tppCertData.setCity(getValueFromX500Name(x500name, BCStyle.L));
            tppCertData.setState(getValueFromX500Name(x500name, BCStyle.ST));
            tppCertData.setCountry(getValueFromX500Name(x500name, BCStyle.C));
            tppCertData.setName(getValueFromX500Name(x500name, BCStyle.CN));

        } catch (CertificateEncodingException e) {
            log.debug(e.getMessage());
            throw new CertificateValidationException(CertificateErrorMsgCode.CERTIFICATE_INVALID.toString());
        }

        tppCertData.setDnsList(getSubjectAltNames(cert));
        return tppCertData;

    }

    private static String getValueFromX500Name(X500Name x500Name, ASN1ObjectIdentifier asn1ObjectIdentifier) {
        boolean exist = ArrayUtils.contains(x500Name.getAttributeTypes(), asn1ObjectIdentifier);
        return exist ? IETFUtils.valueToString(x500Name.getRDNs(asn1ObjectIdentifier)[0].getFirst().getValue()) : null;
    }

    private static String extractIssuerCNFromIssuerDN(Principal issuerDN) {
        List<Rdn> rdns = getRdns(issuerDN);
        return rdns.stream()
                   .filter(rdn -> LDAP_COMMON_NAME.equalsIgnoreCase(rdn.getType()))
                   .findFirst()
                   .filter(rdn -> rdn.getValue() instanceof String)
                   .map(rdn -> (String) rdn.getValue())
                   .orElse(null);
    }

    private static List<Rdn> getRdns(Principal issuerDN) {
        return Optional.ofNullable(issuerDN)
                   .map(Principal::getName)
                   .map(CertificateExtractorUtil::getLdapName)
                   .map(LdapName::getRdns)
                   .orElseGet(Collections::emptyList);
    }

    private static LdapName getLdapName(String dn) {
        try {
            return new LdapName(dn);
        } catch (InvalidNameException e) {
            log.error("Error extracting issuer cn from dn: {}", dn);
            return null;
        }
    }

    private static List<String> getSubjectAltNames(X509Certificate certificate) {
        try {
            Collection<?> subjectAltNames = certificate.getSubjectAlternativeNames();
            if (CollectionUtils.isEmpty(subjectAltNames)) {
                return Collections.emptyList();
            }

            return certificate.getSubjectAlternativeNames().stream()
                       .map(List.class::cast)
                       .filter(CertificateExtractorUtil::isValidSubjectAltName)
                       .filter(e -> (Integer) e.get(0) == GeneralName.dNSName)
                       .map(e -> (String) e.get(1))
                       .filter(Objects::nonNull)
                       .collect(Collectors.toList());
        } catch (CertificateParsingException e) {
            return Collections.emptyList();
        }
    }

    private static boolean isValidSubjectAltName(List<?> entry) {
        return entry != null
                   && entry.size() >= 2
                   && entry.get(0) != null;
    }
}
