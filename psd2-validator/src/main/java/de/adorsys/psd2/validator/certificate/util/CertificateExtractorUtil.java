package de.adorsys.psd2.validator.certificate.util;

import java.io.IOException;
import java.security.cert.X509Certificate;

import org.apache.commons.lang3.ArrayUtils;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.x509.extension.X509ExtensionUtil;

import com.nimbusds.jose.util.X509CertUtils;

public class CertificateExtractorUtil {

	
	public static TppCertData extract(String encodedCert) {

		X509Certificate cert = X509CertUtils.parse(encodedCert);
		
		//NPMD TODO: extract PSD2 attributes inside certificate by their OIDs
		
		String [] roles = {};
		
		TppCertData tppCertData = new TppCertData();
		tppCertData.setPspName(cert.getSubjectDN().getName());
		tppCertData.setPspAuthorityCountry("Germany");
		tppCertData.setPspAuthorityName("ALam");
		tppCertData.setPspAuthorzationNumber("AUTnum1223");
		
		byte[] v = cert.getExtensionValue(Extension.subjectAlternativeName.getId());
		GeneralNames gn;
		try {
			gn = GeneralNames.getInstance(X509ExtensionUtil.fromExtensionValue(v));
			GeneralName[] names = gn.getNames();
			for (GeneralName name : names) {
			    if (name.getTagNo() == GeneralName.otherName) {
			        ASN1Sequence seq = ASN1Sequence.getInstance(name.getName());
			        ASN1ObjectIdentifier oid = (ASN1ObjectIdentifier)seq.getObjectAt(0);
			        if ("1.2.3.1".equals(oid.getId())) {
			            ASN1Integer value = (ASN1Integer) seq.getObjectAt(1);
			            int number = value.getValue().intValue();
			            if(number == 1) {
			            	roles = ArrayUtils.add(roles, "AISP");
			            }
			        }
			        if ("1.2.3.2".equals(oid.getId())) {
			            ASN1Integer value = (ASN1Integer) seq.getObjectAt(1);
			            int number = value.getValue().intValue();
			            if(number == 1) {
			            	roles = ArrayUtils.add(roles, "PISP");
			            }
			        }
			        if ("1.2.3.3".equals(oid.getId())) {
			            ASN1Integer value = (ASN1Integer) seq.getObjectAt(1);
			            int number = value.getValue().intValue();
			            if(number == 1) {
			            	roles = ArrayUtils.add(roles, "PIISP");
			            }
			        }
			    }
			}
			tppCertData.setPspRoles(roles);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return tppCertData;

	}
}
