package de.adorsys.psd2.validator.certificate;

import java.security.cert.X509Certificate;

import no.difi.certvalidator.api.FailedValidationException;
import no.difi.certvalidator.rule.ExpirationRule;

public class ExpirationRuleExt extends ExpirationRule {

	public void validate(X509Certificate certificate) throws FailedCertValidationException {

		try {
			super.validate(certificate);
		} catch (FailedValidationException e) {
			throw new FailedCertValidationException(CertificateErrorMsgCode.CERTIFICATE_EXPIRED.name(),
					CertificateErrorMsgCode.CERTIFICATE_EXPIRED.toString());
		}
	}
}
