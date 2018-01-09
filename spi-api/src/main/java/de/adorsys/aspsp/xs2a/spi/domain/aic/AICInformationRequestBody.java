package de.adorsys.aspsp.xs2a.spi.domain.aic;


import de.adorsys.aspsp.xs2a.spi.domain.SingleAccountAccess;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by aro on 27.11.17.
 */
@Data
@ApiModel(description = "Request Body for created by some methods in the AIC Service")
public class AICInformationRequestBody  {
 
	@ApiModelProperty(value = "Requested access service per account.", required=true)
    private SingleAccountAccess[] accounts;
	@ApiModelProperty(value = "This parameter is requesting a valid until date for the requested consent. The content is the local ASPSP date in ISODate Format", required=true, example="2017-10-30")
	private String valid_until;
	@ApiModelProperty(value = "This field indicates the requested maximum frequency for an access per day. For a once-off access, this attribute is set to 1", required=true, example="4")
	private Integer frequency_per_day;
	@ApiModelProperty(value = "\"true\", if the consent is for recurring access to the account data \"false\", if the consent is for one access to the account data", required=true)
	private boolean recurring_indicator;
	@ApiModelProperty(value = "If \"true\" indicates that a payment initiation service will be addressed in the same \"session\"", required=true)
	private boolean combined_service_indicator;
	

}


