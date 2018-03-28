package de.adorsys.aspsp.xs2a.spi.domain.account;

import de.adorsys.aspsp.xs2a.spi.domain.common.SpiAmount;
import lombok.Data;

import java.util.Date;

@Data
public class SpiAccountBalance {
    private SpiAmount spiAmount;
    private Date lastActionDateTime;
    private Date date;
}
