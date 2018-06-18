package de.adorsys.psd2.validator.certificate.util;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.x509.extension.X509ExtensionUtil;

import com.nimbusds.jose.util.X509CertUtils;

public class CertificateExtractorUtil {

	public static TppCertificateData extract(String encodedCert) throws IOException {

		X509Certificate cert = X509CertUtils.parse(encodedCert);

		List<TppRole> roles = new ArrayList<>();

		TppCertificateData tppCertData = new TppCertificateData();
		tppCertData.setPspName(cert.getSubjectDN().getName());

		// NPMD TODO: extract PSD2 attributes inside certificate by their
		// correct OIDs
		// https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/139
		/*
		 * tppCertData.setPspAuthorityCountry("Germany");
		 * tppCertData.setPspAuthorityName("ALam");
		 * tppCertData.setPspAuthorzationNumber("AUTnum1223");
		 */

		byte[] extValues = cert.getExtensionValue(Extension.subjectAlternativeName.getId());
		GeneralNames gn;
		gn = GeneralNames.getInstance(X509ExtensionUtil.fromExtensionValue(extValues));
		GeneralName[] names = gn.getNames();
		for (GeneralName name : names) {
			if (name.getTagNo() == GeneralName.otherName) {
				ASN1Sequence seq = ASN1Sequence.getInstance(name.getName());
				String roleObjectIdentifier = ((ASN1ObjectIdentifier) seq.getObjectAt(0)).getId();
				int valueObjectIdentifier = ((ASN1Integer) seq.getObjectAt(1)).getValue().intValue();

				if (valueObjectIdentifier == 1) {
					if (TppCertificateOID.PSP_ROLE_AISP_OID.equals(roleObjectIdentifier)) {
						roles.add(TppRole.AISP);
					}
					if (TppCertificateOID.PSP_ROLE_PISP_OID.equals(roleObjectIdentifier)) {
						roles.add(TppRole.PISP);
					}
					if (TppCertificateOID.PSP_ROLE_PIISP_OID.equals(roleObjectIdentifier)) {
						roles.add(TppRole.PIISP);
					}
				}
			}
		}

		tppCertData.setPspRoles(roles);

		return tppCertData;

	}
}
