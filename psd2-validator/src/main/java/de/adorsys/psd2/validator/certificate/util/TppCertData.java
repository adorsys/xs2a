package de.adorsys.psd2.validator.certificate.util;

import java.util.List;

import lombok.Data;

@Data
public class TppCertData {

	private String pspAuthorzationNumber;
	
	private List<TppRole> pspRoles;
	
	private String pspName;
	
	private String pspAuthorityName;
	
	private String pspAuthorityCountry;
	
}
