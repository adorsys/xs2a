/*
 * Copyright 2018-2024 adorsys GmbH & Co KG
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
 * contact us at sales@adorsys.com.
 */

package de.adorsys.psd2.consent.api.pis.proto;

import de.adorsys.psd2.xs2a.core.pis.InternalPaymentStatus;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.NotificationSupportedMode;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.core.tpp.TppRedirectUri;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;

@Data
public class PisPaymentInfo {
    @Schema(description = "External Payment Id", example = "32454656712432")
    private String paymentId;

    @Schema(description = "Payment product", required = true, example = "sepa-credit-transfers")
    private String paymentProduct;

    @Schema(description = "Payment type: BULK, SINGLE or PERIODIC.", required = true, example = "SINGLE")
    private PaymentType paymentType;

    @Schema(name = "transactionStatus", example = "ACCP")
    private TransactionStatus transactionStatus;

    @Schema(name = "internalPaymentStatus", example = "INIT")
    private InternalPaymentStatus internalPaymentStatus;

    @Schema(description = "Payment data")
    private byte[] paymentData;

    @Schema(description = "Tpp information", required = true)
    private TppInfo tppInfo;

    @Schema(description = "Corresponding PSU list", required = true)
    private List<PsuIdData> psuDataList;

    @Schema(description = "Defines whether the payment requires multilevel SCA", example = "true")
    private boolean multilevelScaRequired;

    @Schema(description = "Aspsp-Account-ID: Bank specific account ID", example = "26bb59a3-2f63-4027-ad38-67d87e59611a")
    private String aspspAccountId;

    @Schema(description = "Timestamp of the last payment transaction status changing")
    private OffsetDateTime statusChangeTimestamp;

    @Schema(description = "Tpp redirect URI object'")
    private TppRedirectUri tppRedirectUri;

    @Schema(description = "Internal request ID")
    private String internalRequestId;

    @Schema(description = "Timestamp of the payment creation")
    private OffsetDateTime creationTimestamp;

    @Schema(description = "Tpp notification URI")
    private String tppNotificationUri;

    @Schema(description = "List of notification modes. It could be values: SCA, PROCESS, LAST or NONE ")
    private List<NotificationSupportedMode> notificationSupportedModes;

    @Schema(description = "Response content type")
    private String contentType;

    @Schema(description = "Tpp brand logging information")
    private String tppBrandLoggingInformation;

    @Schema(description = "Instance ID")
    private String instanceId;
}
