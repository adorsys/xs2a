package de.adorsys.psd2.validator.certificate;

import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

public class CertificateValidatorFactory {

	private List<CertificateValidator> certValidators = new ArrayList<>();

	public CertificateValidatorFactory(String truststore, BlockedCertificateIf blockedCertificateImpl) {
		certValidators.add(new CertificateMissingValidator());
		certValidators.add(new CertificateBlockedValidator(blockedCertificateImpl));
		certValidators.add(new CertificateExpiredValidator());
		certValidators.add(new CertificateInvalidValidator(truststore));
		certValidators.add(new CertificateRevokedValidator(truststore));
	}

	public List<CertificateValidator> getCertValidators() {
		return certValidators;
	}

	public void setCertValidators(List<CertificateValidator> certValidators) {
		this.certValidators = certValidators;
	}
	
	public boolean validate(String encodedCert) throws CertificateException {
		
		for(CertificateValidator certValidator:certValidators) {
			certValidator.validate(encodedCert);
		}
		
		return true;
		
	}

}
