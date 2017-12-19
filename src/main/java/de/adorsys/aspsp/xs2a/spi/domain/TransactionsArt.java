package de.adorsys.aspsp.xs2a.spi.domain;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.annotations.ApiModel;

/**
 * Created by aro on 13.12.17.
 */
@JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)

@ApiModel(description="The type of transactions", value="TransactionsArt" )
public enum TransactionsArt {
	 booked, expected, authorised, opening_booked, closing_booked, interim_available;
}
