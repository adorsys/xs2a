package de.adorsys.aspsp.xs2a.spi.domain;

import lombok.Data;

@Data
public class AIRequestHeader extends AIGeneralHeader{

	private String accessToken;
	private String date;
	private String certificate;
	private String signature;
	private String signatureData;
	private String psuId;
	private String psuCorporateId;
	
	private String psuIpAdress;
	
}
