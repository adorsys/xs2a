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

package de.adorsys.psd2.consent.api.pis.proto;

import de.adorsys.psd2.xs2a.core.pis.InternalPaymentStatus;
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

    @ApiModelProperty(name = "internalPaymentStatus", example = "INIT")
    private InternalPaymentStatus internalPaymentStatus;

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

    @ApiModelProperty(value = "Response content type")
    private String contentType;

    @ApiModelProperty(value = "Tpp brand logging information")
    private String tppBrandLoggingInformation;

    @ApiModelProperty(value = "Instance ID")
    private String instanceId;
}
