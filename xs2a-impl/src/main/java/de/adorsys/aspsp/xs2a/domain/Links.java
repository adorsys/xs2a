package de.adorsys.aspsp.xs2a.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "Links ", value = "Links")
@JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
public class Links {

    @ApiModelProperty(value = "redirect: A link to an ASPSP site where SCA is performed within the Redirect SCA approach.", example = "https://www.testbank.com/authentication/1234-wertiq-983")
    private String redirect;

    @ApiModelProperty(value = "The link refers to a JSON document specifying the OAuth details of the ASPSP’s authorisation server.", example = "https://www.testbank.com/oauth/.well-known/oauth- authorization-server")
    private String oAuth;

    @ApiModelProperty(value = "The link to the payment initiation or account information resource, which needs to be updated by the PSU identification if not delivered yet.", example = "api/v1/consents/1234-wertiq-983")
    private String updatePsuIdentification;

    @ApiModelProperty(value = "The link to the payment initiation or account information resource, which needs to be updated by the proprietary data.", example = "api/v1/identification/")
    private String updateProprietaryData;

    @ApiModelProperty(value = "The link to the payment initiation or account information resource, which needs to be updated by a PSU password and eventually the PSU identification if not delivered yet.", example = "api/v1/payments/sepa-credit-transfers/1234-wertiq-983")
    private String updatePsuAuthentication;

    @ApiModelProperty(value = "This is a link to a resource, where the TPP can select the applicable second factor authentication methods for the PSU, if there were several available authentication methods.", example = "api/v1/oauth2/")
    private String selectAuthenticationMethod;

    @ApiModelProperty(value = "Self: The link to the payment initiation resource created by the request itself. This link can be used later to retrieve the transaction status of the payment initiation.", example = "api/v1/payments/sepa-credit-transfers/1234-wertiq-983")
    private String self;

    @ApiModelProperty(value = "Link for check the status of a transaction", example = "https://api.testbank.com/v1/payments/sepa-credit-transfers/qwer3456tzui7890/status")
    private String status;

    @ApiModelProperty(value = "account link", example = "api/v1/accounts/3dc3d5b3-7023-4848-9853-f5400a64e80f")
    private String viewAccount;

    @ApiModelProperty(value = "balances: A link to the resource providing the balance of a dedicated account.", example = "api/v1/accounts/3dc3d5b3-7023-4848-9853-f5400a64e80f/balances")
    private String viewBalances;

    @ApiModelProperty(value = "TransactionsCreditorResponse: A link to the resource providing the transaction history of a dediated amount.", example = "api/v1/accounts/3dc3d5b3-7023-4848-9853-f5400a64e80f/transactions")
    private String viewTransactions;

    @ApiModelProperty(value = "Navigation link for paginated account reports.", example = "api/v1/firstPage/")
    private String first;

    @ApiModelProperty(value = "Navigation link for paginated account reports.", example = "api/v1/nextPage/")
    private String next;

    @ApiModelProperty(value = "Navigation link for paginated account reports.", example = "api/v1/previousPage/")
    private String previous;

    @ApiModelProperty(value = "Navigation link for paginated account reports.", example = "api/v1/lastPage/")
    private String last;

    @ApiModelProperty(value = "download: link to a resource, where the transaction report might be downloaded when is requested which has a huge size", example = "/v1/accounts/12345678999/transactions/download/")
    private String download;
}
