
package de.adorsys.aspsp.xs2a.spi.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Currency;

@Data
@ApiModel(description = "AccountDetails information", value = "AccountDetails")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccountDetails {

    @ApiModelProperty(value = "ID: This is the data element to be used in the path when retrieving data from a dedicated account", required = true, example = "12345")
    @Size(max = 35)
    @NotNull
    private final String id;

    @ApiModelProperty(value = "IBAN: This data element can be used in the body of the CreateConsentReq Request Message for retrieving account access consent from this payment accoun", example = "DE2310010010123456760")
    private final String iban;

    @ApiModelProperty(value = "BBAN: This data element can be used in the body of the CreateConsentReq Request Message for retrieving account access consent from this account, for payment accounts which have no IBAN. ", example = "1111111111")
    private final String bban;

    @ApiModelProperty(value = "PAN: Primary Account Number (PAN) of a card, can be tokenized by the ASPSP due to PCI DSS requirements", example = "1111")
    @Size(max = 35)
    private final String pan;

    @ApiModelProperty(value = "MSISDN: An alias to access a payment account via a registered mobile phone number.", example = "123456xxxxxx1234")
    @Size(max = 35)
    private final String maskedPan;

    @ApiModelProperty(value = "MSISDN: An alias to access a payment account via a registered mobile phone number.", example = "0172/1111111")
    @Size(max = 35)
    private final String msisdn;

    @ApiModelProperty(value = "Currency Type", required = true, example = "EUR")
    @NotNull
    private final Currency currency;

    @ApiModelProperty(value = "Name: Name given by the bank or the Psu in Online- Banking", example = "lily")
    private final String name;

    @ApiModelProperty(value = "Account Type: Product Name of the Bank for this account", example = "SCT")
    @Size(max = 35)
    private final String accountType;

    @ApiModelProperty(value = "Cash Account Type: PExternalCashAccountType1Code from ISO20022", example = "CurrentAccount")
    private final CashAccountType cashAccountType;

    @ApiModelProperty(value = "BIC: The BIC associated to the account.", example = "1234567890")
    private final String bic;

    @ApiModelProperty(value = "Balances")
    private final Balances balances;

    @ApiModelProperty(value = "links: inks to the account, which can be directly used for retrieving account information from the dedicated account")
    private final Links _links;

    /**
     * method updates balance and transactions links with 'id' , example: /v1/accounts/3dc3d5b3-7023-4848-9853- f5400a64e80f/balances
     *
     * @param urlToController main account controller url
     */
    public void setBalanceAndTransactionLinksDyDefault(String urlToController) {
        String urlWithId = urlToController + "/" + id;
        String balancesLink = urlWithId + "/balances";
        String transactionsLink = urlWithId + "/transactions";
        _links.setViewBalances(balancesLink);
        _links.setViewTransactions(transactionsLink);
    }
}
