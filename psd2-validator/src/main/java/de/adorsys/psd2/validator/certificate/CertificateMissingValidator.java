package de.adorsys.psd2.validator.certificate;

import java.security.cert.CertificateException;

import org.apache.commons.lang3.StringUtils;

import de.adorsys.psd2.validator.exception.CertificateMissingException;

public class CertificateMissingValidator implements CertificateValidator {

	@Override
	public void validate(String encodedCert) throws CertificateException {

		if (StringUtils.isBlank(encodedCert)) {
			throw new CertificateMissingException(CertificateErrorMessageCode.CERTIFICATE_MISSING.name(),
					CertificateErrorMessageCode.CERTIFICATE_MISSING.toString());
		}
	}

}
