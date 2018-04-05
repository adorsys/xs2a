package de.adorsys.aspsp.xs2a.spi.domain.payment;

import lombok.Data;

import java.util.Date;

@Data
public class SpiPeriodicPayment extends SpiSinglePayments {

    private Date startDate;
    private Date endDate;
    private String executionRule;
    private String frequency;
    private int dayOfExecution; //Day here max 31
}
