package de.adorsys.aspsp.xs2a.domain;

import java.util.List;
import java.util.Map;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by aro on 23.11.17.
 */

@Data

public class PSU_User {
	 
	private String psu_id; 
    private String psu_message;
	private String psu_password;
	private String psu_corporate_id;
	private String psu_authentification;
	private String psu_IP_adress;
	private String psu_agent;
	private String psu_geo_location;
}
