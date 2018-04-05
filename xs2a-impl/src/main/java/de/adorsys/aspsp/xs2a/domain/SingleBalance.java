package de.adorsys.aspsp.xs2a.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.Instant;

@Data
@ApiModel(description = "Balance Information", value = "SingleBalance")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SingleBalance {

    @ApiModelProperty(value = "amount", required = true)
	private Amount amount;

	@ApiModelProperty(value = "last action date time", example = "2017-10-25T15:30:35.035Z")
	@JsonFormat(pattern = ApiDateConstants.DATE_TIME_PATTERN, timezone = ApiDateConstants.UTC)
	private Instant lastActionDateTime;

	@ApiModelProperty(value = "Date", example = "2017-03-26")
	@JsonFormat(pattern = ApiDateConstants.DATE_PATTERN, timezone = ApiDateConstants.UTC)
	private Instant date;
}
