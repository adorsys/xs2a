package de.adorsys.aspsp.xs2a.domain.pis;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import de.adorsys.aspsp.xs2a.domain.Amount;
import de.adorsys.aspsp.xs2a.domain.Links;
import de.adorsys.aspsp.xs2a.domain.MessageCode;
import de.adorsys.aspsp.xs2a.domain.TransactionStatus;
import de.adorsys.aspsp.xs2a.domain.ais.consent.AuthenticationObject;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "Request creates payment initiation regarding payment id specified in request")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreatePaymentInitiationRequest {

    @ApiModelProperty(value = "Can be used by the ASPSP to transport transaction fees relevant for the underlying payments.")
    private final Amount transactionFees;

    @ApiModelProperty(value = "If equals true, the transaction will involve specific transaction cost as shown by the ASPSP in their public price list or as agreed between ASPSP and PSU.", example="false")
    private final boolean transactionFeeIndicator;

    @ApiModelProperty(value = "This data element might be contained, if SCA is required and if the PSU has a choice between different authentication methods")
    private final AuthenticationObject[] scaMethods;

    @ApiModelProperty(value = "Text to be displayed to the PSU")
    private final String psuMessage;

    @ApiModelProperty(value = "Messages to the TPP on operational issues.")
    private final MessageCode[] tppMessages;

}
