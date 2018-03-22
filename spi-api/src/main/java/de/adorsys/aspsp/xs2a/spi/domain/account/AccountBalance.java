package de.adorsys.aspsp.xs2a.spi.domain.account;

import lombok.Data;

import de.adorsys.aspsp.xs2a.spi.domain.common.Amount;

import java.util.Date;

@Data
public class AccountBalance {

    private Amount amount;

    private Date lastActionDateTime;

    private Date date;
}
