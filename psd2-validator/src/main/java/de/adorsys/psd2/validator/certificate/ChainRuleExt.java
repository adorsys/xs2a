package de.adorsys.psd2.validator.certificate;

import java.security.cert.X509Certificate;

import no.difi.certvalidator.api.CertificateBucket;
import no.difi.certvalidator.api.CertificateValidationException;
import no.difi.certvalidator.rule.ChainRule;

public class ChainRuleExt extends ChainRule {

	public ChainRuleExt(CertificateBucket rootCertificates, CertificateBucket intermediateCertificates,
			String[] policies) {
		super(rootCertificates, intermediateCertificates, policies);
	}
	
	public ChainRuleExt(CertificateBucket rootCertificates, CertificateBucket intermediateCertificates) {
		super(rootCertificates, intermediateCertificates);
	}

	public void validate(X509Certificate certificate) throws FailedCertValidationException {

		try {
			super.validate(certificate);
		} catch (CertificateValidationException e) {
			throw new FailedCertValidationException(CertificateErrorMsgCode.CERTIFICATE_INVALID.name(),
					CertificateErrorMsgCode.CERTIFICATE_INVALID.toString());
		}
	}

}
