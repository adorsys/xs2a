package de.adorsys.psd2.validator.certificate;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;

import com.nimbusds.jose.util.X509CertUtils;

import de.adorsys.psd2.validator.exception.CertificateInvalidException;

public class CertificateInvalidValidator implements CertificateValidator {

	private static final String TRUSTSTORE_PATH = "resources/truststore";

	@Override
	public void validate(String encodedCert) throws CertificateException {

		X509Certificate cert = X509CertUtils.parse(encodedCert);
		
		//NOPMD TODO check if certificate contain psd2 attributes
		//cert.getExtendedKeyUsage().containsAll(listPsd2RequiredAttributes);

		List<X509Certificate> listRootCertificate = CertificateUtils.getRootCertificate(TRUSTSTORE_PATH);

		for (X509Certificate rootCA : listRootCertificate) {
			if (rootCA.getSubjectDN().equals(cert.getIssuerDN())) {
				try {
					cert.verify(rootCA.getPublicKey());
					return;
				} catch (InvalidKeyException | CertificateException | NoSuchAlgorithmException | NoSuchProviderException
						| SignatureException e) {
					throw new CertificateInvalidException(CertificateErrorMessageCode.CERTIFICATE_INVALID.name(),
							CertificateErrorMessageCode.CERTIFICATE_INVALID.toString());
				}
			}
		}
		
		throw new CertificateInvalidException(CertificateErrorMessageCode.CERTIFICATE_INVALID.name(),
				CertificateErrorMessageCode.CERTIFICATE_INVALID.toString());
	}

}
