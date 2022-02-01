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

package de.adorsys.psd2.certificate.generator.service;

import de.adorsys.psd2.certificate.generator.exception.CertificateGeneratorException;
import de.adorsys.psd2.certificate.generator.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERUTF8String;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.qualified.QCStatement;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class CertificateService {
    private static final String NCA_SHORT_NAME = "FAKENCA";
    private static final ASN1ObjectIdentifier ETSI_QC_STATEMENT = new ASN1ObjectIdentifier("0.4.0.19495.2");
    private static final SecureRandom random = new SecureRandom();

    private final IssuerDataService issuerDataService;

    /**
     * Create a new base64 encoded X509 certificate for authentication at the XS2A API with the
     * corresponding private key and meta data.
     *
     * @param certificateRequest data needed for certificate generation
     * @return CertificateResponse base64 encoded cert + private key
     */
    public CertificateResponse newCertificate(CertificateRequest certificateRequest) {
        SubjectData subjectData = generateSubjectData(certificateRequest);
        QCStatement qcStatement = generateQcStatement(certificateRequest);

        X509Certificate cert = generateCertificate(subjectData, qcStatement);

        return CertificateResponse.builder()
                   .privateKey(ExportUtil.exportToString(subjectData.getPrivateKey()))
                   .encodedCert(ExportUtil.exportToString(cert))
                   .build();
    }

    /**
     * Generates new X.509 Certificate
     *
     * @return X509Certificate
     */
    private X509Certificate generateCertificate(SubjectData subjectData, QCStatement statement) {
        JcaContentSignerBuilder builder = new JcaContentSignerBuilder("SHA256WithRSAEncryption");
        IssuerData issuerData = issuerDataService.getIssuerData();
        ContentSigner contentSigner;

        X509v3CertificateBuilder certGen = new JcaX509v3CertificateBuilder(issuerData.getX500name(),
                                                                           new BigInteger(subjectData.getSerialNumber().toString()), subjectData.getStartDate(),
                                                                           subjectData.getEndDate(),
                                                                           subjectData.getX500name(), subjectData.getPublicKey());

        JcaX509CertificateConverter certConverter;

        try {
            contentSigner = builder.build(issuerData.getPrivateKey());
            certGen.addExtension(Extension.qCStatements, false, statement);

            X509CertificateHolder certHolder = certGen.build(contentSigner);

            certConverter = new JcaX509CertificateConverter();

            return certConverter.getCertificate(certHolder);
        } catch (Exception ex) {
            throw new CertificateGeneratorException("Could not create certificate", ex);
        }
    }

    private QCStatement generateQcStatement(CertificateRequest certificateRequest) {
        NcaName ncaName = getNcaNameFromIssuerData();
        NcaId ncaId = getNcaIdFromIssuerData();
        ASN1Encodable qcStatementInfo = createQcInfo(
            RolesOfPsp.fromCertificateRequest(certificateRequest), ncaName, ncaId
        );

        return new QCStatement(ETSI_QC_STATEMENT, qcStatementInfo);
    }

    private DERSequence createQcInfo(RolesOfPsp rolesOfPsp, NcaName ncaName, NcaId ncaId) {
        return new DERSequence(new ASN1Encodable[]{rolesOfPsp, ncaName, ncaId});
    }

    private NcaName getNcaNameFromIssuerData() {
        return new NcaName(IETFUtils.valueToString(
            issuerDataService.getIssuerData().getX500name().getRDNs(BCStyle.O)[0]
                .getFirst().getValue())
        );
    }

    private NcaId getNcaIdFromIssuerData() {
        String country = IETFUtils.valueToString(issuerDataService.getIssuerData()
                                                     .getX500name().getRDNs(BCStyle.C)[0]
                                                     .getFirst().getValue());
        return new NcaId(country + "-" + NCA_SHORT_NAME);
    }

    private SubjectData generateSubjectData(CertificateRequest cerData) {
        X500NameBuilder builder = new X500NameBuilder(BCStyle.INSTANCE);
        builder.addRDN(BCStyle.O, cerData.getOrganizationName());
        if (StringUtils.isNotBlank(cerData.getCommonName())) {
            builder.addRDN(BCStyle.CN, cerData.getCommonName());
        }
        if (cerData.getDomainComponent() != null) {
            builder.addRDN(BCStyle.DC, cerData.getDomainComponent());
        }
        if (cerData.getOrganizationUnit() != null) {
            builder.addRDN(BCStyle.OU, cerData.getOrganizationUnit());
        }
        if (cerData.getCountryName() != null) {
            builder.addRDN(BCStyle.C, cerData.getCountryName());
        }
        if (cerData.getStateOrProvinceName() != null) {
            builder.addRDN(BCStyle.ST, cerData.getStateOrProvinceName());
        }
        if (cerData.getLocalityName() != null) {
            builder.addRDN(BCStyle.L, cerData.getLocalityName());
        }

        builder.addRDN(BCStyle.ORGANIZATION_IDENTIFIER,
                       "PSD" + getNcaIdFromIssuerData() + "-" + cerData.getAuthorizationNumber());

        Date expiration = Date.from(
            LocalDate.now().plusDays(cerData.getValidity()).atStartOfDay(ZoneOffset.UTC).toInstant()
        );
        KeyPair keyPairSubject = generateKeyPair();
        Integer serialNumber = random.nextInt(Integer.MAX_VALUE);
        return new SubjectData(
            keyPairSubject.getPrivate(), keyPairSubject.getPublic(), builder.build(),
            serialNumber, new Date(), expiration
        );
    }

    private KeyPair generateKeyPair() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048, SecureRandom.getInstance("SHA1PRNG", "SUN"));
            return keyGen.generateKeyPair();
        } catch (GeneralSecurityException ex) {
            throw new CertificateGeneratorException("Could not generate key pair", ex);
        }
    }

    private static class RolesOfPsp extends DERSequence {

        static RolesOfPsp fromCertificateRequest(CertificateRequest certificateRequest) {
            List<RoleOfPsp> roles = new ArrayList<>();

            List<PspRole> requestRoles = certificateRequest.getRoles();
            if (requestRoles.contains(PspRole.AISP)) {
                roles.add(RoleOfPsp.PSP_AI);
            }

            if (requestRoles.contains(PspRole.PISP)) {
                roles.add(RoleOfPsp.PSP_PI);
            }

            if (requestRoles.contains(PspRole.PIISP)) {
                roles.add(RoleOfPsp.PSP_IC);
            }

            return new RolesOfPsp(roles.toArray(new RoleOfPsp[]{}));
        }

        RolesOfPsp(RoleOfPsp... array) {
            super(array);
        }
    }

    private static class RoleOfPsp extends DERSequence {

        static final RoleOfPsp PSP_PI = new RoleOfPsp(RoleOfPspOid.ID_PSD_2_ROLE_PSP_PI,
                                                      RoleOfPspName.PSP_PI);
        static final RoleOfPsp PSP_AI = new RoleOfPsp(RoleOfPspOid.ID_PSD_2_ROLE_PSP_AI,
                                                      RoleOfPspName.PSP_AI);
        static final RoleOfPsp PSP_IC = new RoleOfPsp(RoleOfPspOid.ROLE_OF_PSP_OID,
                                                      RoleOfPspName.PSP_IC);

        private RoleOfPsp(RoleOfPspOid roleOfPspOid, RoleOfPspName roleOfPspName) {
            super(new ASN1Encodable[]{roleOfPspOid, roleOfPspName});
        }
    }

    private static class RoleOfPspName extends DERUTF8String {
        static final RoleOfPspName PSP_PI = new RoleOfPspName("PSP_PI");
        static final RoleOfPspName PSP_AI = new RoleOfPspName("PSP_AI");
        static final RoleOfPspName PSP_IC = new RoleOfPspName("PSP_IC");

        private RoleOfPspName(String string) {
            super(string);
        }
    }

    private static class RoleOfPspOid extends ASN1ObjectIdentifier {

        static final ASN1ObjectIdentifier ETSI_PSD_2_ROLES = new ASN1ObjectIdentifier(
            "0.4.0.19495.1");
        static final RoleOfPspOid ID_PSD_2_ROLE_PSP_PI = new RoleOfPspOid(
            ETSI_PSD_2_ROLES.branch("2"));
        static final RoleOfPspOid ID_PSD_2_ROLE_PSP_AI = new RoleOfPspOid(
            ETSI_PSD_2_ROLES.branch("3"));
        static final RoleOfPspOid ROLE_OF_PSP_OID = new RoleOfPspOid(
            ETSI_PSD_2_ROLES.branch("4"));

        RoleOfPspOid(ASN1ObjectIdentifier identifier) {
            super(identifier.getId());
        }
    }
}
