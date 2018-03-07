package de.adorsys.aspsp.xs2a.spi.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@ApiModel(description = "Balance Information", value = "SingleBalance")
@JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
public class SingleBalance {

	@ApiModelProperty(value = "amount", required = true)
	private Amount amount;

	@ApiModelProperty(value = "last action date time", example = "2017-10-25T15:30:35.035Z")
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
	private Date lastActionDateTime;

	@ApiModelProperty(value = "Date", example = "2007-01-01")
	@JsonFormat(pattern = "yyyy-MM-dd")
	private Date date;
}
