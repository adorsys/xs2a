package de.adorsys.aspsp.xs2a.spi.domain;


import java.util.Date;

import lombok.Data;

/**
 * Created by aro on 27.11.17.
 */
@Data
public class AIC  {
  
	private TPP tpp;
	
	private String access_token;
	private Date request_timestamp;
	
	private Authentification[] sca_methods;
    private Authentification  chosen_sca_method;
    private Challange sca_challange_data;
    private String psu_id;
    private Account psu_account;
    private SingleAccountAccess[] accounts;
	private String valid_until;
	private Integer frequency_per_day;
	private boolean recurring_indicator;
	private boolean combined_service_indicator;

}


