package de.adorsys.aspsp.xs2a.domain;

import lombok.Data;

@Data
public class PsuUser extends Psu {

	private String psu_id;
	private String psu_message;
	private String psu_corporate_id;
	private String psu_authentication;
	private String psu_IP_address;
	private String psu_agent;
	private String psu_geo_location;
}
