package de.adorsys.psd2.validator.certificate;

import java.security.cert.X509Certificate;

import no.difi.certvalidator.api.CertificateValidationException;
import no.difi.certvalidator.rule.CRLRule;

public class CRLRuleExt extends CRLRule {

	public void validate(X509Certificate certificate) throws FailedCertValidationException {

		try {
			super.validate(certificate);
		} catch (CertificateValidationException e) {
			throw new FailedCertValidationException(CertificateErrorMsgCode.CERTIFICATE_REVOKED.name(),
					CertificateErrorMsgCode.CERTIFICATE_REVOKED.toString());
		}
	}
}
