package de.adorsys.aspsp.xs2a.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import de.adorsys.aspsp.xs2a.domain.TppMessageInformation;
import de.adorsys.aspsp.xs2a.domain.TransactionStatus;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MessageError {
    @ApiModelProperty(value = "Transaction status", example = "Rejected")
    private TransactionStatus transactionStatus;

    @ApiModelProperty(value = "Tpp messages information of the Berlin Group XS2A Interface")
    private TppMessageInformation tppMessage;

    public MessageError(TppMessageInformation tppMessage) {
        this(TransactionStatus.RJCT, tppMessage);
    }

    public MessageError(TransactionStatus status, TppMessageInformation tppMessage) {
        this.transactionStatus = status;
        this.tppMessage = tppMessage;
    }
}
