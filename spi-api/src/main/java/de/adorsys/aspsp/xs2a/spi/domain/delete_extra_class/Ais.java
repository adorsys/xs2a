package de.adorsys.aspsp.xs2a.spi.domain.delete_extra_class;

import de.adorsys.aspsp.xs2a.spi.domain.*;
import de.adorsys.aspsp.xs2a.spi.domain.ais.consents.AuthenticationObject;
import lombok.Data;

import java.util.Date;

@Data
public class Ais {
	private Tpp tpp;
	private String access_token;
	private Date request_timestamp;
	private AuthenticationObject[] sca_methods;
	private AuthenticationObject chosen_sca_method;
	private Challenge sca_challenge_data;
	private PsuUser psu;
	private Account psu_account;
	private SingleAccountAccess[] accounts;
	private Date date_from;
	private Date date_to;
	private String valid_until;
	private Integer frequency_per_day;
	private boolean recurring_indicator;
	private boolean combined_service_indicator;
}


