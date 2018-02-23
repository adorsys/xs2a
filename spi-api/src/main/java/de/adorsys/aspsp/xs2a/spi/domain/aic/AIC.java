package de.adorsys.aspsp.xs2a.spi.domain.aic;


import de.adorsys.aspsp.xs2a.spi.domain.*;
import lombok.Data;

import java.util.Date;

/**
 * Created by aro on 27.11.17.
 */
@Data
public class AIC {

    private TPP tpp;
    private String access_token;
    private Date request_timestamp;
    private Authentication[] sca_methods;
    private Authentication chosen_sca_method;
    private Challenge sca_challenge_data;
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


