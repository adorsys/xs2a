package de.adorsys.psd2.validator.certificate;

import java.security.cert.X509Certificate;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;



public class CertificateUtilsTest {
	
	@Test
	public void test_getRootCertificate() {
		
		CertificateUtils certificateUtils = new CertificateUtils();
		List<X509Certificate> rootCertList = certificateUtils.getRootCertificate("truststore");
		Assert.assertTrue(rootCertList.size() >= 1);
	}
	
	@Test
	public void test_getCertificateByName() {
		
		CertificateUtils certificateUtils = new CertificateUtils();
		String encodedCert = certificateUtils.getCertificateByName("certificateValid.crt");
		Assert.assertTrue(StringUtils.isNotBlank(encodedCert));
	}

}
