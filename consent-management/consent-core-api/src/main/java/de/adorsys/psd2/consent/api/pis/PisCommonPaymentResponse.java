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

package de.adorsys.psd2.consent.api.pis;

import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.pis.InternalPaymentStatus;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
@Schema(description = "Pis payment initialisation common payment response", name = "PisCommonPaymentResponse")
public class PisCommonPaymentResponse implements CommonPaymentData {
    @Schema(description = "Payment data", required = true)
    private List<PisPayment> payments;

    @Schema(description = "Payment product", required = true, example = "sepa-credit-transfers")
    private String paymentProduct;

    @Schema(description = "Payment type: BULK, SINGLE or PERIODIC.", required = true, example = "SINGLE")
    private PaymentType paymentType;

    @Schema(description = "Tpp information", required = true)
    private TppInfo tppInfo;

    @Schema(description = "An external exposed identification of the created common payment", required = true, example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7")
    private String externalId;

    @Schema(description = "List of corresponding PSU", required = true)
    private List<PsuIdData> psuData;

    @Schema(description = "Payment info")
    private byte[] paymentData;

    @Schema(description = "Transaction status", required = true)
    private TransactionStatus transactionStatus;

    @Schema(description = "Internal payment status", required = true)
    private InternalPaymentStatus internalPaymentStatus;

    @Schema(description = "Timestamp of the last payment transaction status changing")
    private OffsetDateTime statusChangeTimestamp;

    @Schema(description = "List of corresponding PSU", required = true)
    private List<Authorisation> authorisations = new ArrayList<>();

    @Schema(description = "Defines whether the payment requires multilevel SCA", example = "true")
    private boolean multilevelScaRequired;

    @Schema(description = "Timestamp of the payment creation")
    private OffsetDateTime creationTimestamp;

    @Schema(description = "Response content type")
    private String contentType;

    @Schema(description = "Response instance id")
    private String instanceId;

    @Schema(description = "Signing basket blocked")
    private boolean signingBasketBlocked;

    @Schema(description = "Signing basket authorised")
    private boolean signingBasketAuthorised;

    public Optional<Authorisation> findAuthorisationInPayment(String authorisationId) {
        return authorisations.stream()
                   .filter(auth -> auth.getAuthorisationId().equals(authorisationId))
                   .findFirst();
    }
}
