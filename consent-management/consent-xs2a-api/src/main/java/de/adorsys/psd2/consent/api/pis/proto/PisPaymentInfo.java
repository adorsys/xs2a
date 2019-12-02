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

package de.adorsys.psd2.consent.api.pis.proto;

import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.NotificationSupportedMode;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.core.tpp.TppRedirectUri;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;

@Data
public class PisPaymentInfo {
    @ApiModelProperty(value = "External Payment Id", example = "32454656712432")
    private String paymentId;

    @ApiModelProperty(value = "Payment product", required = true, example = "sepa-credit-transfers")
    private String paymentProduct;

    @ApiModelProperty(value = "Payment type: BULK, SINGLE or PERIODIC.", required = true, example = "SINGLE")
    private PaymentType paymentType;

    @ApiModelProperty(name = "transactionStatus", example = "ACCP")
    private TransactionStatus transactionStatus;

    @ApiModelProperty(value = "Payment data")
    private byte[] paymentData;

    @ApiModelProperty(value = "Tpp information", required = true)
    private TppInfo tppInfo;

    @ApiModelProperty(value = "Corresponding PSU list", required = true)
    private List<PsuIdData> psuDataList;

    @ApiModelProperty(value = "Defines whether the payment requires multilevel SCA", example = "true")
    private boolean multilevelScaRequired;

    @ApiModelProperty(value = "Aspsp-Account-ID: Bank specific account ID", example = "26bb59a3-2f63-4027-ad38-67d87e59611a")
    private String aspspAccountId;

    @ApiModelProperty(value = "Timestamp of the last payment transaction status changing")
    private OffsetDateTime statusChangeTimestamp;

    @ApiModelProperty(value = "Tpp redirect URI object'")
    private TppRedirectUri tppRedirectUri;

    @ApiModelProperty(value = "Internal request ID")
    private String internalRequestId;

    @ApiModelProperty(value = "Timestamp of the payment creation")
    private OffsetDateTime creationTimestamp;

    @ApiModelProperty(value = "Tpp notification URI")
    private String tppNotificationUri;

    @ApiModelProperty(value = "List of notification modes. It could be values: SCA, PROCESS, LAST or NONE ")
    private List<NotificationSupportedMode> notificationSupportedModes;
}
