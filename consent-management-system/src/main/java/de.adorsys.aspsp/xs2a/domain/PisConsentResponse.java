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

package de.adorsys.aspsp.xs2a.domain;

import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiConsentStatus;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Value;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Date;

@Value
@ApiModel(description = "Pis consent response entity", value = "PisConsentResponse")
public class PisConsentResponse {
    @ApiModelProperty(value = "An external exposed identification of the created payment consent", required = true, example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7")
    private String externalId;

    @ApiModelProperty(value = "Iban of the debtor", required = true, example = "DE2310010010123")
    private String debtorIban;

    @ApiModelProperty(value = "Name of the debtor", required = true, example = "Mueller")
    private String ultimateDebtor;

    @ApiModelProperty(value = "Iso currency code", required = true, example = "EUR")
    private Currency currency;

    @ApiModelProperty(value = "Payment amount", required = true, example = "1000")
    private BigDecimal amount;

    @ApiModelProperty(value = "Iban of the creditor", required = true, example = "DE2310010010123")
    private String creditorIban;

    @ApiModelProperty(value = "Creditor agent", required = true, example = "Telekom")
    private String creditorAgent;

    @ApiModelProperty(value = "Name of the creditor", required = true, example = "Telekom")
    private String creditorName;

    @ApiModelProperty(value = "Requested execution date", required = true, example = "2017-01-01")
    private Date requestedExecutionDate;

    @ApiModelProperty(value = "Requested execution time", required = true, example = "2017-10-25T15:30:35.035Z")
    private Date requestedExecutionTime;

    @ApiModelProperty(value = "The following code values are permitted 'received', 'valid', 'rejected', 'expired', 'revoked by psu', 'terminated by tpp'. These values might be extended by ASPSP.", required = true, example = "VALID")
    private SpiConsentStatus consentStatus;
}
