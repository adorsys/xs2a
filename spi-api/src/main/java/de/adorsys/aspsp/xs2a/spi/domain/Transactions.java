package de.adorsys.aspsp.xs2a.spi.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * Created by aro on 23.11.17.
 */

@Data
@ApiModel(description = "TransactionsCreditorResponse information", value = "TransactionsCreditorResponse")
@JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
public class Transactions {

    @ApiModelProperty(value = "Transaction ID: Can be used as access-id in the API, where more details on an transaction is offered", example = "12345")
    // we get it in the Header in the Prozess-ID
    private String transaction_id;

    // we get it in the Header
    @ApiModelProperty(value = "Entry Date", example = "2017-01-01")
    private Date entry_date;

    @ApiModelProperty(value = "Booking Date", example = "2017-01-01")
    private Date booking_date;

    @ApiModelProperty(value = "Value Date", example = "2017-01-01")
    private Date value_date;

    @ApiModelProperty(value = "Amount", required = true)
    private Amount amount;

    @ApiModelProperty(value = "Credited or Debited", required = true, example = "Credited or Debited")
    private String credit_debit;

    @ApiModelProperty(value = "Name of the Creditor if a debited transaction", example = "Bauer")
    private String creditor_name;

    @ApiModelProperty(value = "Creditor account", example = "56666")
    private Account creditor_account;

    @ApiModelProperty(value = "Name of the last creditor", example = "Max")
    private String ultimate_creditor;

    @ApiModelProperty(value = "Name of the Debtor if a credited transaction", example = "Jan")
    private String debtor;

    @ApiModelProperty(value = "Debtor account", example = "56666")
    private Account debtor_account;

    @ApiModelProperty(value = "Name of the last debtor", example = "Max")
    private String ultimate_debtor;

    @ApiModelProperty(value = "Remittance information", example = "Otto")
    private String remittance_information_unstructured;
}
