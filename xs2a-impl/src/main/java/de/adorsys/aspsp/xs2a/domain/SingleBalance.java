package de.adorsys.aspsp.xs2a.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import de.adorsys.aspsp.xs2a.web.util.JsonFormatDateTimeUTC;
import de.adorsys.aspsp.xs2a.web.util.JsonFormatDateUTC;
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
	@JsonFormatDateTimeUTC
	private Instant lastActionDateTime;

	@ApiModelProperty(value = "Date", example = "2017-03-26")
	@JsonFormatDateUTC
	private Instant date;
}
