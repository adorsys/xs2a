package de.adorsys.aspsp.xs2a.spi.domain;

import io.swagger.annotations.ApiModel;

/**
 * Created by aro on 13.12.17.
 */
@ApiModel(description="The type of transactions", value="TransactionsArt" )
public enum TransactionsArt {
	 booked, expected, authorised, opening_booked, closing_booked, interim_available;
}
