package de.adorsys.psd2.validator.certificate.util;

import java.security.cert.X509Certificate;

import com.nimbusds.jose.util.X509CertUtils;

public class CertificateExtractorUtil {

	
	public static TppCertData extract(String encodedCert) {

		X509Certificate cert = X509CertUtils.parse(encodedCert);
		
		//NPMD TODO: extract PSD2 attributes inside certificate by their OIDs
		
		String [] roles = {"AISP","PIISP", "PISP"};
		
		TppCertData tppCertData = new TppCertData();
		tppCertData.setPspName(cert.getSubjectDN().getName());
		tppCertData.setPspAuthorityCountry("Germany");
		tppCertData.setPspAuthorityName("ALam");
		tppCertData.setPspAuthorzationNumber("AUTnum1223");
		tppCertData.setPspRoles(roles);
		
		return tppCertData;

	}
}
