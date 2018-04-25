package de.adorsys.psd2.validator.certificate;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import com.nimbusds.jose.util.X509CertUtils;

public class CertificateExpiredValidator  implements CertificateValidator{

	
	@Override
	public void validate(String encodedCert) throws CertificateException {

		X509Certificate cert = X509CertUtils.parse(encodedCert);
		cert.checkValidity();
	}

}
