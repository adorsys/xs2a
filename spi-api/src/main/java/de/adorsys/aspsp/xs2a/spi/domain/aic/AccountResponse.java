package de.adorsys.aspsp.xs2a.spi.domain.aic;

import de.adorsys.aspsp.xs2a.spi.domain.Balances;
import de.adorsys.aspsp.xs2a.spi.domain.Links;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;


/**
 * Created by aro on 23.11.17.
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "Account Response information", value = "AccountResponse")
public class AccountResponse {

    @ApiModelProperty(value = "ID: This is the data element to be used in the path when retrieving data from a dedicated account", required = true, example = "12345")
    private String id;

    @ApiModelProperty(value = "IBAN: This data element can be used in the body of the AICInformationRequestBody Request Message for retrieving account access consent from this payment accoun", example = "1111111111")
    private String iban;

    @ApiModelProperty(value = "Account Type: Product Name of the Bank for this account", example = "SCT")
    private String account_type;

    @ApiModelProperty(value = "Currency Type", required = true, example = "€")
    private String currency;

    @ApiModelProperty(value = "Balances")
    private Balances balances;

    @ApiModelProperty(value = "links: inks to the account, which can be directly used for retrieving account information from the dedicated account")
    private Links _links;
}
