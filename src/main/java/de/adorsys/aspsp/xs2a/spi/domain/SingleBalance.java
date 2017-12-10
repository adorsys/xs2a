package de.adorsys.aspsp.xs2a.spi.domain;

import java.util.Date;
import java.util.List;
import java.util.Map;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by aro on 23.11.17.
 */

@Data
@ApiModel(description = "Balance Information", value = "SingleBalance")
public class SingleBalance {
	
	@ApiModelProperty(value = "amount", required=true)
	 private Amount amount;

	@ApiModelProperty(value = "last action date time", example = "2007-01-01T17:30:12.000")
	 private Date last_action_date_time;
	
	@ApiModelProperty(value = "Date", example = "2007-01-01")
	 private Date date;
	
}
