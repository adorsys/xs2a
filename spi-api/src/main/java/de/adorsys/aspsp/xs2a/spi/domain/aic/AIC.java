package de.adorsys.aspsp.xs2a.spi.domain.aic;


import java.util.Date;

import de.adorsys.aspsp.xs2a.spi.domain.Account;
import de.adorsys.aspsp.xs2a.spi.domain.Authentification;
import de.adorsys.aspsp.xs2a.spi.domain.Challange;
import de.adorsys.aspsp.xs2a.spi.domain.PSU_User;
import de.adorsys.aspsp.xs2a.spi.domain.SingleAccountAccess;
import de.adorsys.aspsp.xs2a.spi.domain.TPP;
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
    private PSU_User psu;
    private Account psu_account;
    private SingleAccountAccess[] accounts;
    private Date date_from;
    private Date date_to;
	private String valid_until;
	private Integer frequency_per_day;
	private boolean recurring_indicator;
	private boolean combined_service_indicator;

}


