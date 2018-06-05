package de.adorsys.psd2.validator.certificate.util;

import lombok.Data;

@Data
public class TppCertData {

	private String pspAuthorzationNumber;
	
	private String [] pspRoles;
	
	private String pspName;
	
	private String pspAuthorityName;
	
	private String pspAuthorityCountry;
	
}
