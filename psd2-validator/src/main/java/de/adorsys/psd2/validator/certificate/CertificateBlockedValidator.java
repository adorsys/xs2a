package de.adorsys.psd2.validator.certificate;

import java.math.BigInteger;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import com.nimbusds.jose.util.X509CertUtils;

import de.adorsys.psd2.validator.exception.CertificateBlockedException;

public class CertificateBlockedValidator implements CertificateValidator {

	private BlockedCertificateIf blockedCertImpl;

	public CertificateBlockedValidator(BlockedCertificateIf blockedCertImpl) {
		super();
		this.blockedCertImpl = blockedCertImpl;
	}

	@Override
	public void validate(String encodedCert) throws CertificateException {

		X509Certificate cert = X509CertUtils.parse(encodedCert);
		BigInteger serialNumber = cert.getSerialNumber();

		if (blockedCertImpl.getBlockedCertNbers().contains(serialNumber)) {
			throw new CertificateBlockedException(CertificateErrorMessageCode.CERTIFICATE_BLOCKED.name(),
					CertificateErrorMessageCode.CERTIFICATE_BLOCKED.toString());
		}

	}

}