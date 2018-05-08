package de.adorsys.psd2.validator.certificate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DummyBlockedCert implements BlockedCertificateIf {
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private List<String> dummyBlockedCertList = new ArrayList(Arrays.asList("1.3.6.1.4.1.21528.2.2.99.11534x", "xxxxxxxx"));

	@Override
	public List<String> getBlockedCertNbers() {
		return dummyBlockedCertList;
	}

	@Override
	public void addBlockedCertNber(String certNber) {

		dummyBlockedCertList.add(certNber);
	}

}
