package de.adorsys.aspsp.xs2a.spi.domain.aic;


import de.adorsys.aspsp.xs2a.spi.domain.TransactionStatus;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by aro on 27.11.17.
 */
@Data
@ApiModel(description = "Response for the AIC information  request in the AICService")
public class AICStatusResponseBody extends AICGeneralResponseBody {

    @ApiModelProperty(value = "Transaction status", required = true)
    private TransactionStatus transactions_status;
}


