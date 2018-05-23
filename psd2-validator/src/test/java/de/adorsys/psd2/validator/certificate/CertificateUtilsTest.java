package de.adorsys.psd2.validator.certificate;

import java.security.cert.X509Certificate;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;



public class CertificateUtilsTest {
	
	@Test
	public void test_getRootCertificate() {
		
		X509Certificate[] rootCertList = CertificateUtils.getCertificates("rootcert");
		Assert.assertTrue(rootCertList.length >= 1);
	}
	
	@Test
	public void test_getCertificateByName() {
		
		String encodedCert = CertificateUtils.getCertificateByName("certificateValid.crt");
		Assert.assertTrue(StringUtils.isNotBlank(encodedCert));
	}

}
