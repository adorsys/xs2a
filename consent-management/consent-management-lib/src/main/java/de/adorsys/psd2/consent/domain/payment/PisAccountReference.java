/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.psd2.consent.domain.payment;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.*;
import java.util.Currency;

@Data
@Entity(name = "pis_account_reference")
@ApiModel(description = "Pis account reference", value = "Pis account reference")
public class PisAccountReference {
    @Id
    @Column(name = "account_reference_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pis_account_reference_generator")
    @SequenceGenerator(name = "pis_account_reference_generator", sequenceName = "pis_account_reference_id_seq")
    private Long id;

    @ApiModelProperty(value = "IBAN: This data element can be used in the body of the CreateConsentReq Request Message for retrieving account access consent from this payment account", example = "DE89370400440532013000")
    private String iban;

    @ApiModelProperty(value = "BBAN: This data elements is used for payment accounts which have no IBAN", example = "89370400440532013000")
    private String bban;

    @ApiModelProperty(value = "PAN: Primary Account Number (PAN) of a card, can be tokenized by the ASPSP due to PCI DSS requirements.", example = "2356 5746 3217 1234")
    private String pan;

    @Column(name = "masked_pan")
    @ApiModelProperty(value = "MASKEDPAN: Primary Account Number (PAN) of a card in a masked form.", example = "2356xxxxxxxx1234")
    private String maskedPan;

    @ApiModelProperty(value = "MSISDN: An alias to access a payment account via a registered mobile phone number. This alias might be needed e.g. in the payment initiation service, cp. Section 5.3.1. The support of this alias must be explicitly documented by the ASPSP for the corresponding API calls.", example = "+49(0)911 360698-0")
    private String msisdn;

    @ApiModelProperty(value = "Codes following ISO 4217", example = "EUR")
    private Currency currency;
}
