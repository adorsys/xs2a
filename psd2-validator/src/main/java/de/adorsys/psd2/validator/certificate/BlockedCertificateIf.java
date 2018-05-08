package de.adorsys.psd2.validator.certificate;

import java.util.List;

public interface BlockedCertificateIf {

	List<String> getBlockedCertNbers();
	void addBlockedCertNber(String certNber);
}
