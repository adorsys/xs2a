package de.adorsys.psd2.validator.certificate;

import java.util.ArrayList;
import java.util.List;

public class CertificateValidatorFactory {

	private List<CertificateValidator> certValidators = new ArrayList<>();

	public CertificateValidatorFactory() {
		super();
		certValidators.add(new CertificateBlockedValidator());
		certValidators.add(new CertificateExpiredValidator());
		certValidators.add(new CertificateInvalidValidator());
		certValidators.add(new CertificateMissingValidator());
		certValidators.add(new CertificateRevokedValidator());
	}

	public List<CertificateValidator> getCertValidators() {
		return certValidators;
	}

	public void setCertValidators(List<CertificateValidator> certValidators) {
		this.certValidators = certValidators;
	}
	
	

}
