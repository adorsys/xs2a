/*
 * Copyright 2018-2022 adorsys GmbH & Co KG
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version. This program is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 *
 * This project is also available under a separate commercial license. You can
 * contact us at psd2@adorsys.com.
 */

package de.adorsys.psd2.xs2a.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
@ApiModel(description = "Links ", value = "Links")
public class Links {

    @ApiModelProperty(value = "The link to an ASPSP site where SCA is performed within the Redirect SCA approach.", example = "https://www.testbank.com/authentication/1234-wertiq-983")
    private HrefType scaRedirect;

    @ApiModelProperty(value = "The link refers to a JSON document specifying the OAuth details of the ASPSP’s authorisation server.", example = "https://www.testbank.com/oauth/.well-known/oauth- authorization-server")
    private HrefType scaOAuth;

    @ApiModelProperty(value = "The link to the payment initiation or account information resource, which needs to be updated by the PSU identification if not delivered yet.", example = "api/v1/consents/1234-wertiq-983")
    private HrefType updatePsuIdentification;

    @ApiModelProperty(value = "The link to the payment initiation or account information resource, which needs to be updated by the proprietary data.", example = "api/v1/identification/")
    private HrefType updateProprietaryData;

    @ApiModelProperty(value = "The link to the payment initiation or account information resource, which needs to be updated by a PSU password and eventually the PSU identification if not delivered yet.", example = "api/v1/payments/sepa-credit-transfers/1234-wertiq-983")
    private HrefType updatePsuAuthentication;

    @ApiModelProperty(value = "This is a link to a resource, where the TPP can select the applicable second factor authentication methods for the PSU, if there were several available authentication methods.", example = "api/v1/oauth2/")
    private HrefType selectAuthenticationMethod;

    @ApiModelProperty(value = "Self: The link to the payment initiation resource created by the request itself. This link can be used later to retrieve the transaction status of the payment initiation.", example = "api/v1/payments/sepa-credit-transfers/1234-wertiq-983")
    private HrefType self;

    @ApiModelProperty(value = "Link for check the status of a transaction", example = "https://api.testbank.com/v1/payments/sepa-credit-transfers/qwer3456tzui7890/status")
    private HrefType status;

    @ApiModelProperty(value = "account link", example = "api/v1/accounts/3dc3d5b3-7023-4848-9853-f5400a64e80f")
    private HrefType account;

    @ApiModelProperty(value = "balances: A link to the resource providing the balance of a dedicated account.", example = "api/v1/accounts/3dc3d5b3-7023-4848-9853-f5400a64e80f/balances")
    private HrefType balances;

    @ApiModelProperty(value = "TransactionsCreditorResponse: A link to the resource providing the transaction history of a dediated amount.", example = "api/v1/accounts/3dc3d5b3-7023-4848-9853-f5400a64e80f/transactions")
    private HrefType transactions;

    @ApiModelProperty(value = "Navigation link for paginated account reports.", example = "api/v1/firstPage/")
    private HrefType first;

    @ApiModelProperty(value = "Navigation link for paginated account reports.", example = "api/v1/nextPage/")
    private HrefType next;

    @ApiModelProperty(value = "Navigation link for paginated account reports.", example = "api/v1/previousPage/")
    private HrefType previous;

    @ApiModelProperty(value = "Navigation link for paginated account reports.", example = "api/v1/lastPage/")
    private HrefType last;

    @ApiModelProperty(value = "download: link to a resource, where the transaction report might be downloaded when is requested which has a huge size", example = "/v1/accounts/12345678999/transactions/download/")
    private HrefType download;

    @ApiModelProperty(value = "In case, where an explicit start of the transaction authorisation is needed, but no more data needs to be updated (no authentication method to be selected, no PSU identification nor PSU authentication data to be uploaded)")
    private HrefType startAuthorisation;

    @ApiModelProperty(value = "The link to the authorisation end-point, where the authorisation sub-resource has to be generated while uploading the PSU identification data.")
    private HrefType startAuthorisationWithPsuIdentification;

    @ApiModelProperty(value = "The link to the authorisation end-point, where the authorisation sub-resource has to be generated while uploading the PSU authentication data.")
    private HrefType startAuthorisationWithPsuAuthentication;

    @ApiModelProperty(value = "The link to the authorisation end-point, where the authorisation sub-resource has to be generated while selecting the authentication method. This link is contained under exactly the same conditions as the data element \"scaMethods\"")
    private HrefType startAuthorisationWithAuthenticationMethodSelection;//NOPMD naming according to spec!

    @ApiModelProperty(value = "The link to the authorisation end-point, where the authorisation sub-resource has to be generated while authorising the transaction e.g. by uploading an OTP received by SMS.")
    private HrefType startAuthorisationWithTransactionAuthorisation;

    @ApiModelProperty(value = "The link to retrieve the scaStatus of the corresponding authorisation sub-resource. This link is only contained, if an authorisation sub-resource has been already created.")
    private HrefType scaStatus;

    @ApiModelProperty(value = "The link to the authorisation or cancellation authorisation sub-resource, where the authorisation data has to be uploaded, e.g. the TOP received by SMS.")
    private HrefType authoriseTransaction;

    @ApiModelProperty(value = "The link, which should be used by for storing the confirmation code in CMS.")
    private HrefType confirmation;

    @ApiModelProperty(value = "The link for Card Account Report data type")
    private HrefType card;
}
