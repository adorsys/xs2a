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

package de.adorsys.psd2.consent.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.*;
import java.util.Currency;

@Data
@Entity(name = "account_reference")
@ApiModel(description = "Account reference", value = "Account reference")
public class AccountReferenceEntity {
    @Id
    @Column(name = "account_reference_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "account_reference_generator")
    @SequenceGenerator(name = "account_reference_generator", sequenceName = "account_reference_id_seq", allocationSize = 1)
    private Long id;

    @ApiModelProperty(value = "IBAN: This data element can be used in the body of the CreateConsentReq Request Message for retrieving account access consent from this payment account", example = "DE89370400440532013000")
    private String iban;

    @ApiModelProperty(value = "BBAN: This data elements is used for payment accounts which have no IBAN", example = "89370400440532013000")
    private String bban;

    @ApiModelProperty(value = "PAN: Primary Account Number (PAN) of a card, can be tokenized by the ASPSP due to PCI DSS requirements.", example = "2356 5746 3217 1234")
    private String pan;

    @Column(name = "masked_pan")
    @ApiModelProperty(value = "MASKED_PAN: Primary Account Number (PAN) of a card in a masked form.", example = "2356xxxxxxxx1234")
    private String maskedPan;

    @ApiModelProperty(value = "MSISDN: An alias to access a payment account via a registered mobile phone number. This alias might be needed e.g. in the payment initiation service, cp. Section 5.3.1. The support of this alias must be explicitly documented by the ASPSP for the corresponding API calls.", example = "+49(0)911 360698-0")
    private String msisdn;

    @ApiModelProperty(value = "Codes following ISO 4217", example = "EUR")
    private Currency currency;

    @Column(name = "aspsp_account_id", length = 100)
    @ApiModelProperty(value = "Aspsp-Account-ID: Bank specific account ID", example = "26bb59a3-2f63-4027-ad38-67d87e59611a")
    private String aspspAccountId;
}
