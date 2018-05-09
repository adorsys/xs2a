package de.adorsys.psd2.validator.certificate;

import java.io.IOException;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;

import com.nimbusds.jose.util.X509CertUtils;

import de.adorsys.psd2.validator.certificate.ocsp.OCSP;
import de.adorsys.psd2.validator.certificate.ocsp.OCSP.RevocationStatus;
import de.adorsys.psd2.validator.exception.CertificateInvalidException;

public class CertificateRevokedValidator implements CertificateValidator {
	
	private final String TRUSTSTORE_PATH;

	public CertificateRevokedValidator(String tRUSTSTORE_PATH) {
		super();
		TRUSTSTORE_PATH = tRUSTSTORE_PATH;
	}


	@Override
	public void validate(String encodedCert) throws CertificateException {
		
		X509Certificate cert = X509CertUtils.parse(encodedCert);

		List<X509Certificate> listRootCertificate = new CertificateUtils().getRootCertificate(TRUSTSTORE_PATH);

		for (X509Certificate rootCA : listRootCertificate) {
			if (rootCA.getSubjectDN().equals(cert.getIssuerDN())) {
				try {
					RevocationStatus revocationStatus = OCSP.check(cert, rootCA);
					if(revocationStatus.getCertStatus().equals(RevocationStatus.CertStatus.GOOD)) {
						return ;
					}else {
						
						throw new CertificateInvalidException(CertificateErrorMessageCode.CERTIFICATE_REVOKED.name(),
								CertificateErrorMessageCode.CERTIFICATE_REVOKED.toString());
					}
					
				} catch (CertPathValidatorException | IOException e) {
					e.printStackTrace();
					throw new CertificateInvalidException(CertificateErrorMessageCode.CERTIFICATE_REVOKED.name(),
							CertificateErrorMessageCode.CERTIFICATE_REVOKED.toString());
				}
			}
		}
	}

}
