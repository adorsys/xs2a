package de.adorsys.aspsp.xs2a.spi.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonRootName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@ApiModel(description = "AccountResponse Report", value = "AccountReport")
@JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
@JsonRootName(value = "transactions")
public class AccountReport {
    
    @ApiModelProperty(value = "Booked TransactionsCreditorResponse", required = true)
    private Transactions[] booked;
    
    @ApiModelProperty(value = "Pending TransactionsCreditorResponse")
    private Transactions[] pending;
    
    @ApiModelProperty(value = "Links: he following links might be used within this context:" +
                              "account link (mandatory)" +
                              "first_page_link (optional)" +
                              "second_page_link (optional)" +
                              "current_page_ link (optional)" +
                              "last_page_link (optional)", required = true)
    private Links _links;
}
