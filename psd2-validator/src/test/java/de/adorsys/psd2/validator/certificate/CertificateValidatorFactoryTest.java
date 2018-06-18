package de.adorsys.psd2.validator.certificate;

import java.security.cert.CertificateException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.adorsys.psd2.validator.certificate.util.CertificateUtils;
import no.difi.certvalidator.api.CertificateValidationException;
import no.difi.certvalidator.util.SimpleCertificateBucket;

public class CertificateValidatorFactoryTest {

	private SimpleCertificateBucket blockedCertBucket;
	private SimpleCertificateBucket rootCertBucket;
	private SimpleCertificateBucket intermediateCertBucket;

	@Before
	public void init() {

		blockedCertBucket = new SimpleCertificateBucket(CertificateUtils.getCertificates("blockedcert"));
		rootCertBucket = new SimpleCertificateBucket(CertificateUtils.getCertificates("rootcert", "TCA3.crt"));
		intermediateCertBucket = new SimpleCertificateBucket(CertificateUtils.getCertificates("intermediatecert"));
	}

	@Test
	public void when_ValidCertificate_Expected_True() throws CertificateException, CertificateValidationException {

		String encodedCert = CertificateUtils.getCertificateByName("certificateValid.crt");

		CertificateValidatorFactory validatorFactory = new CertificateValidatorFactory(blockedCertBucket,
				rootCertBucket, intermediateCertBucket);

		Assert.assertTrue(validatorFactory.validate(encodedCert));
	}

	@Test(expected = CertificateValidationException.class)
	public void when_InValidCertificate_Expected_Exception()
			throws CertificateException, CertificateValidationException {

		String encodedCert = CertificateUtils.getCertificateByName("certificateInvalid.crt");

		CertificateValidatorFactory validatorFactory = new CertificateValidatorFactory(blockedCertBucket,
				rootCertBucket, intermediateCertBucket);

		validatorFactory.validate(encodedCert);
	}
}
