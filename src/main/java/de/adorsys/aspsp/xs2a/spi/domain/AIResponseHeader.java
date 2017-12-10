package de.adorsys.aspsp.xs2a.spi.domain;

import lombok.Data;

@Data
public class AIResponseHeader extends AIGeneralHeader{
	private String transaction_status;
	private String psu_message;
	private String tpp_messages;
	
}
