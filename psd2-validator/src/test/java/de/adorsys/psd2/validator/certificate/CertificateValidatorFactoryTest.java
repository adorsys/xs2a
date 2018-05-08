package de.adorsys.psd2.validator.certificate;

import java.security.cert.CertificateException;

import org.junit.Assert;
import org.junit.Test;

import de.adorsys.psd2.validator.exception.CertificateBlockedException;
import de.adorsys.psd2.validator.exception.CertificateExpiredException;
import de.adorsys.psd2.validator.exception.CertificateInvalidException;
import de.adorsys.psd2.validator.exception.CertificateMissingException;
import de.adorsys.psd2.validator.exception.CertificateRevokedException;

public class CertificateValidatorFactoryTest {
	
	@Test
	public void when_ValidCertificate_Expected_true() throws CertificateException {
		
		CertificateUtils certificateUtils = new CertificateUtils();
		String encodedCert = certificateUtils.getCertificateByName("certificateValid.crt");
		
		CertificateValidatorFactory validatorFactory = new CertificateValidatorFactory("truststore", new DummyBlockedCert());
		Assert.assertTrue(validatorFactory.validate(encodedCert));
	}
	
	@Test(expected = CertificateInvalidException.class)
	public void when_InvalidCertificate_Expected_CertificateInvalidException() throws CertificateException {
		
		CertificateUtils certificateUtils = new CertificateUtils();
		String encodedCert = certificateUtils.getCertificateByName("certificateInvalid.crt");
		
		CertificateValidatorFactory validatorFactory = new CertificateValidatorFactory("truststore", new DummyBlockedCert());
		Assert.assertTrue(validatorFactory.validate(encodedCert));
	}
	
	@Test(expected = CertificateMissingException.class)
	public void when_MissingCertificate_Expected_CertificateMissingException() throws CertificateException {
		
		CertificateUtils certificateUtils = new CertificateUtils();
		String encodedCert = "";	
		CertificateValidatorFactory validatorFactory = new CertificateValidatorFactory("truststore", new DummyBlockedCert());
		Assert.assertTrue(validatorFactory.validate(encodedCert));
	}
	
	/*@Test(expected = CertificateBlockedException.class)
	public void when_BlockedCertificate_Expected_CertificateBlockedException() throws CertificateException {
		CertificateUtils certificateUtils = new CertificateUtils();
		String encodedCert = certificateUtils.getCertificateByName("certificateBlocked.crt");
		
		CertificateValidatorFactory validatorFactory = new CertificateValidatorFactory("truststore", new DummyBlockedCert());
		Assert.assertTrue(validatorFactory.validate(encodedCert));
	}
	
	@Test(expected = CertificateExpiredException.class)
	public void when_ExpiredCertificate_Expected_CertificateExpiredException() throws CertificateException {
		CertificateUtils certificateUtils = new CertificateUtils();
		String encodedCert = certificateUtils.getCertificateByName("certificateExpired.crt");
		
		CertificateValidatorFactory validatorFactory = new CertificateValidatorFactory("truststore", new DummyBlockedCert());
		Assert.assertTrue(validatorFactory.validate(encodedCert));
	}	
	
	@Test(expected = CertificateRevokedException.class)
	public void when_RevokedCertificate_Expected_CertificateRevokedException() throws CertificateException {
		
		CertificateUtils certificateUtils = new CertificateUtils();
		String encodedCert = certificateUtils.getCertificateByName("certificateRevoked.crt");
		
		CertificateValidatorFactory validatorFactory = new CertificateValidatorFactory("truststore", new DummyBlockedCert());
		Assert.assertTrue(validatorFactory.validate(encodedCert));
	}*/
	
	

}
