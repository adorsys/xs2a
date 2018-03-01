package de.adorsys.aspsp.xs2a.spi.domain.ais;

import de.adorsys.aspsp.xs2a.spi.domain.TransactionStatus;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "Response for the Ais information  request in the AICService")
public class AisStatusResponseBody extends AisGeneralResponseBody {

	@ApiModelProperty(value = "Transaction status", required = true)
	private TransactionStatus transactions_status;
}


