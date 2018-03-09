package de.adorsys.aspsp.xs2a.spi.pis;

import com.fasterxml.jackson.annotation.JsonFormat;
import de.adorsys.aspsp.xs2a.spi.domain.AccountReference;
import de.adorsys.aspsp.xs2a.spi.domain.Amount;
import de.adorsys.aspsp.xs2a.spi.domain.address.Address;
import de.adorsys.aspsp.xs2a.spi.domain.codes.BICFI;
import de.adorsys.aspsp.xs2a.spi.domain.codes.PurposeCode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Size;
import java.util.Date;

@Data
@ApiModel(description = "Payment Initialisation Request", value = "SinglePayments")
public class SinglePayments {

    @ApiModelProperty(value = "end to end authentication", example = "RI-123456789")
    @Size(max = 35)
    private String endToEndIdentification;

    @ApiModelProperty(value = "debtor account", required = true, example = "")
    private AccountReference debtorAccount;

    @ApiModelProperty(value = "ultimate debtor", example = "Mueller")
    @Size(max = 70)
    private String ultimateDebtor;

    @ApiModelProperty(value = "instructed amount", required = true, example = "“EUR” , “123.50”")
    private Amount instructedAmount;

    @ApiModelProperty(value = "creditor account", required = true, example = "DE23100120020123456789")
    private AccountReference creditorAccount;

    @ApiModelProperty(value = "creditor agent", example = "BCENECEQ")
    private BICFI creditorAgent;

    @ApiModelProperty(value = "creditor name", required = true, example = "Telekom")
    @Size(max = 70)
    private String creditorName;

    @ApiModelProperty(value = "creditor Address")
    private Address creditorAddress;

    @ApiModelProperty(value = "ultimate creditor", example = "Telekom")
    @Size(max = 70)
    private String ultimateCreditor;

    @ApiModelProperty(value = "purpose code")
    private PurposeCode purposeCode;

    @ApiModelProperty(value = "remittance information unstructured", example = "Ref. Number TELEKOM-1222")
    @Size(max = 140)
    private String remittanceInformationUnstructured;

    @ApiModelProperty(value = "remittance information structured", example = "Telekom")
    private Remittance remittanceInformationStructured;

    @ApiModelProperty(value = "requested execution date", example = "2017-01-01")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date requestedExecutionDate;

    @ApiModelProperty(value = "requested execution time", example = "2017-10-25T15:30:35.035Z")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private Date requestedExecutionTime;

}
