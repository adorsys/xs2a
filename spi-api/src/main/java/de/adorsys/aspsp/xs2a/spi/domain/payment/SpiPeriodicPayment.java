package de.adorsys.aspsp.xs2a.spi.domain.payment;

import lombok.Data;

import java.util.Date;

@Data
public class SpiPeriodicPayment extends SpiSinglePayments {

    private Date startDate;
    private String executionRule;
    private Date endDate;
    private String frequency;
    private int dayOfExecution; //Day here max 31
}
