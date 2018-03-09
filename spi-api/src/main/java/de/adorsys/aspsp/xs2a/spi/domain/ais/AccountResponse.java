package de.adorsys.aspsp.xs2a.spi.domain.ais;

import com.fasterxml.jackson.annotation.JsonInclude;
import de.adorsys.aspsp.xs2a.spi.domain.Account;
import de.adorsys.aspsp.xs2a.spi.domain.Balances;
import de.adorsys.aspsp.xs2a.spi.domain.Links;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Currency;

@Data
@ApiModel(description = "Account Response information", value = "AccountResponse")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccountResponse {

    @ApiModelProperty(value = "ID: This is the data element to be used in the path when retrieving data from a dedicated account", required = true, example = "12345")
    private String id;

    @ApiModelProperty(value = "IBAN: This data element can be used in the body of the AccountInformationConsentRequestBody Request Message for retrieving account access consent from this payment accoun", example = "1111111111")
    private String iban;

    @ApiModelProperty(value = "Account Type: Product Name of the Bank for this account", example = "SCT")
    private String account_type;

    @ApiModelProperty(value = "Currency Type", required = true, example = "EUR")
    private Currency currency;

    @ApiModelProperty(value = "Balances")
    private Balances balances;

    @ApiModelProperty(value = "links: inks to the account, which can be directly used for retrieving account information from the dedicated account")
    private Links _links;

    /**
     * Constructor converts Account to own type
     *
     * @param account       account which the response is given for
     * @param urlToAccounts current url for getting account information
     */
    public AccountResponse(Account account, String urlToAccounts) {
        this.id = account.getId();
        this.iban = account.getIban();
        this.account_type = account.getAccount_type();
        this.currency = account.getCurrency();
        this.balances = account.getBalances();
        this._links = getChangedLinksByAccountId(urlToAccounts + "/" + account.getId(), account.get_links());
    }

    private Links getChangedLinksByAccountId(String urlToAccount, Links links) {
        String balancesLink = urlToAccount + "/balances";
        String transactionsLink = urlToAccount + "/transactions";
        links.setViewBalances(balancesLink);
        links.setViewTransactions(transactionsLink);
        return links;
    }
}
