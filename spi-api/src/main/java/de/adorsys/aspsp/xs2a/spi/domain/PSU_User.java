package de.adorsys.aspsp.xs2a.spi.domain;

import lombok.Data;

/**
 * Created by aro on 23.11.17.
 */

@Data
public class PSU_User extends PSU {

    private String psu_id;
    private String psu_message;
    private String psu_corporate_id;
    private String psu_authentication;
    private String psu_IP_adress;
    private String psu_agent;
    private String psu_geo_location;
}
