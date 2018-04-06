package de.adorsys.aspsp.xs2a.domain.ais.consent;

import com.fasterxml.jackson.annotation.JsonInclude;
import de.adorsys.aspsp.xs2a.web.util.JsonFormatDateUTC;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
@ApiModel(description = "Request creates an account information consent resource at the ASPSP regarding access to accounts specified in this request")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateConsentReq {

    @ApiModelProperty(value = "Requested access services.", required = true)
    @NotNull
    private AccountAccess access;

    @ApiModelProperty(value = "'true', if the consent is for recurring access to the account data , 'false', if the consent is for one access to the account data", required = true)
    @NotNull
    private boolean recurringIndicator;

    @ApiModelProperty(value = "This parameter is requesting a valid until date for the requested consent. The content is the local ASPSP date in ISODate Format", required = true, example = "2017-10-30")
    @JsonFormatDateUTC
    @NotNull
    private Date validUntil;

    @ApiModelProperty(value = "This field indicates the requested maximum frequency for an access per day. For a once-off access, this attribute is set to 1", required = true, example = "4")
    @NotNull
    private int frequencyPerDay;

    @ApiModelProperty(value = "If 'true' indicates that a payment initiation service will be addressed in the same 'session'", required = true)
    @NotNull
    private boolean combinedServiceIndicator;
}
