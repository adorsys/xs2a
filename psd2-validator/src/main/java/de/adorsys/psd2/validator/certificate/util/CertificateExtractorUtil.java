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

package de.adorsys.psd2.validator.certificate.util;

import com.nimbusds.jose.util.X509CertUtils;
import de.adorsys.psd2.validator.certificate.CertificateErrorMsgCode;
import de.adorsys.psd2.validator.common.PSD2QCStatement;
import de.adorsys.psd2.validator.common.PSD2QCType;
import de.adorsys.psd2.validator.common.RoleOfPSP;
import de.adorsys.psd2.validator.common.RolesOfPSP;
import lombok.extern.slf4j.Slf4j;
import no.difi.certvalidator.api.CertificateValidationException;
import org.apache.commons.lang3.ArrayUtils;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import java.security.Principal;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
public class CertificateExtractorUtil {
    private static final String LDAP_COMMON_NAME = "CN";

    private CertificateExtractorUtil() {
    }

    public static TppCertificateData extract(String encodedCert) throws CertificateValidationException {

        X509Certificate cert = X509CertUtils.parse(encodedCert);

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
        return tppCertData;

    }

    private static String getValueFromX500Name(X500Name x500Name, ASN1ObjectIdentifier asn1ObjectIdentifier) {
        boolean exist = ArrayUtils.contains( x500Name.getAttributeTypes(), asn1ObjectIdentifier );
        return  exist ? IETFUtils.valueToString(x500Name.getRDNs(asn1ObjectIdentifier)[0].getFirst().getValue()) : null;
    }

    private static String extractIssuerCNFromIssuerDN(Principal issuerDN) {
        List<Rdn> rdns = getRdns(issuerDN);
        return rdns
            .stream()
            .filter(rdn ->  LDAP_COMMON_NAME.equalsIgnoreCase(rdn.getType()))
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


}
