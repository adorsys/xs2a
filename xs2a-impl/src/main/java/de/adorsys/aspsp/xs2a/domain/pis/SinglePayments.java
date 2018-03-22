package de.adorsys.aspsp.xs2a.domain.pis;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import de.adorsys.aspsp.xs2a.domain.AccountReference;
import de.adorsys.aspsp.xs2a.domain.Amount;
import de.adorsys.aspsp.xs2a.domain.ApiDateConstants;
import de.adorsys.aspsp.xs2a.domain.address.Address;
import de.adorsys.aspsp.xs2a.domain.code.BICFI;
import de.adorsys.aspsp.xs2a.domain.code.PurposeCode;

import javax.validation.constraints.Size;
import java.util.Date;

@Data
@ApiModel(description = "Payment Initialisation Request", value = "SinglePayments")
public class SinglePayments {

    @ApiModelProperty(value = "end to end authentication", example = "RI-123456789")
    @Size(max = 35)
    private String endToEndIdentification;

    @ApiModelProperty(value = "debtor account", required = true, example = "'iban': 'DE2310010010123456789'")
    private AccountReference debtorAccount;

    @ApiModelProperty(value = "ultimate debtor", required = false, example = "Mueller")
    @Size(max = 70)
    private String ultimateDebtor;

    @ApiModelProperty(value = "instructed amount", required = true, example = "'EUR' , '123.50'")
    private Amount instructedAmount;

    @ApiModelProperty(value = "creditor account", required = true, example = "'iban': 'DE23100120020123456789'")
    private AccountReference creditorAccount;

    @ApiModelProperty(value = "creditor agent", required = false, example = "BCENECEQ")
    private BICFI creditorAgent;

    @ApiModelProperty(value = "creditor name", required = true, example = "Telekom")
    @Size(max = 70)
    private String creditorName;

    @ApiModelProperty(value = "creditor Address", required = false)
    private Address creditorAddress;

    @ApiModelProperty(value = "ultimate creditor", required = false, example = "Telekom")
    @Size(max = 70)
    private String ultimateCreditor;

    @ApiModelProperty(value = "purpose code", required = false)
    private PurposeCode purposeCode;

    @ApiModelProperty(value = "remittance information unstructured", required = false, example = "Ref. Number TELEKOM-1222")
    @Size(max = 140)
    private String remittanceInformationUnstructured;

    @ApiModelProperty(value = "remittance information structured", required = false, example = "Telekom")
    private Remittance remittanceInformationStructured;

    @ApiModelProperty(value = "requested execution date", required = false, example = "2017-01-01")
    @JsonFormat(pattern = ApiDateConstants.DATE_PATTERN)
    private Date requestedExecutionDate;

    @ApiModelProperty(value = "requested execution time", required = false, example = "2017-10-25T15:30:35.035Z")
    @JsonFormat(pattern = ApiDateConstants.DATE_TIME_PATTERN)
    private Date requestedExecutionTime;

}
