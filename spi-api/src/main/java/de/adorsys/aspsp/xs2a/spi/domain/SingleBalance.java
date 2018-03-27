package de.adorsys.aspsp.xs2a.spi.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
@ApiModel(description = "Balance Information", value = "SingleBalance")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SingleBalance {

    @ApiModelProperty(value = "amount", required = true)
    @NotNull
    private Amount amount;

    @ApiModelProperty(value = "last action date time", required = false, example = "2017-10-25T15:30:35.035Z")
    @JsonFormat(pattern = ApiDateConstants.DATE_TIME_PATTERN)
    private Date lastActionDateTime;

    @ApiModelProperty(value = "Date", required = false, example = "2007-01-01")
    @JsonFormat(pattern = ApiDateConstants.DATE_PATTERN)
    private Date date;
}
