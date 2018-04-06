package de.adorsys.aspsp.xs2a.domain.pis;

import de.adorsys.aspsp.xs2a.domain.code.FrequencyCode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.Date;

@Data
@ApiModel(description = "Periodic Payment Initialisation Request", value = "Periodic Payment")
public class PeriodicPayment extends SinglePayments {

    @ApiModelProperty(name = "startDate", required = true, example = "2017-03-03")
    private Date startDate;
    @ApiModelProperty(name = "executionRule", required = false, example = "preceeding")
    private String executionRule;
    @ApiModelProperty(name = "endDate", required = false, example = "2018-03-03")
    private Date endDate;
    @ApiModelProperty(name = "frequency", required = true, example = "ANNUAL")
    private FrequencyCode frequency;
    @ApiModelProperty(name = "dayOfExecution", required = false, example = "14")
    @Max(31)
    @Min(1)
    private int dayOfExecution; //Day here max 31
}
