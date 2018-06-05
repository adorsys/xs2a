package de.adorsys.psd2.validator.certificate.util;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import org.apache.commons.lang3.StringUtils;

import com.nimbusds.jose.util.X509CertUtils;

import de.adorsys.psd2.validator.certificate.CertificateErrorMsgCode;
import de.adorsys.psd2.validator.certificate.FailedCertValidationException;
import no.difi.certvalidator.api.CertificateValidationException;

public class CertificateExtractorUtil {

	
	public TppCertData extract(String encodedCert) throws CertificateException, CertificateValidationException {

		if (StringUtils.isBlank(encodedCert)) {
			throw new FailedCertValidationException(CertificateErrorMsgCode.CERTIFICATE_MISSING.name(),
					CertificateErrorMsgCode.CERTIFICATE_MISSING.toString());
		}

		X509Certificate cert = X509CertUtils.parse(encodedCert);
		
		//NPMD TODO: extract PSD2 attributes inside certificate by their OIDs
		
		return new TppCertData();

	}
}
