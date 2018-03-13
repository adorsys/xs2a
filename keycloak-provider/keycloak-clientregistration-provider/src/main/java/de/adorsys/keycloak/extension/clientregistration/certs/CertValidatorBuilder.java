package de.adorsys.keycloak.extension.clientregistration.certs;

import de.adorsys.keycloak.extension.clientregistration.certs.rules.CertRule;

import java.util.ArrayList;
import java.util.List;

public class CertValidatorBuilder {

	public List<CertRule> certRules = new ArrayList<>();

	public static CertValidatorBuilder getInstance() {
		return new CertValidatorBuilder();
	}

	public CertValidatorBuilder addRule(CertRule rule) {
		this.certRules.add(rule);
		return this;
	}

	public CertValidator build() {

		return new CertValidator();

	}

}
