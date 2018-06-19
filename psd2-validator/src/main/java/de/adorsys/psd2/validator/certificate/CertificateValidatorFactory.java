package de.adorsys.psd2.validator.certificate;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import org.apache.commons.lang3.StringUtils;

import com.nimbusds.jose.util.X509CertUtils;

import no.difi.certvalidator.Validator;
import no.difi.certvalidator.ValidatorBuilder;
import no.difi.certvalidator.api.CertificateValidationException;
import no.difi.certvalidator.util.SimpleCertificateBucket;

public class CertificateValidatorFactory {

	private Validator validator;

	public CertificateValidatorFactory(SimpleCertificateBucket blockedCertBucket,
			SimpleCertificateBucket rootCertificates, SimpleCertificateBucket intermediateCertificates) {

		validator = ValidatorBuilder.newInstance().addRule(new ExpirationRuleExt()).addRule(new CRLRuleExt())
				.addRule(new BlackListRule(blockedCertBucket))
				.addRule(new ChainRuleExt(rootCertificates, intermediateCertificates)).build();
	}

	public boolean validate(String encodedCert) throws CertificateException, CertificateValidationException {

		if (StringUtils.isBlank(encodedCert)) {
			throw new FailedCertValidationException(CertificateErrorMsgCode.CERTIFICATE_MISSING.name(),
					CertificateErrorMsgCode.CERTIFICATE_MISSING.toString());
		}

		X509Certificate cert = X509CertUtils.parse(encodedCert);

		validator.validate(cert);

		return true;
	}
}
