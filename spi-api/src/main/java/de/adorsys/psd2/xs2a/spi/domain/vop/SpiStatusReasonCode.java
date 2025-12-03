/*
 * Copyright 2018-2025 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.spi.domain.vop;

/**
 * SPI enum representing standardized status reason codes for VOP (Verification of Payee) operations.
 *
 * <p>This enum provides a type-safe representation of status reason codes that explain
 * why a bulk VOP verification is in its current status. These codes follow industry standards
 * and provide consistent, machine-readable reasons for various processing states.</p>
 *
 * <p>Common use cases include:</p>
 * <ul>
 *   <li>Explaining why a verification request was rejected (e.g., AM04 - Insufficient funds)</li>
 *   <li>Providing details about processing issues (e.g., AC06 - Account blocked)</li>
 *   <li>Indicating specific validation failures (e.g., DT01 - Invalid date)</li>
 * </ul>
 *
 * <p>This is the SPI domain representation corresponding to the XS2A API enum
 * {@code StatusReasonCode}. The mapping between SPI and XS2A enums is handled by
 * the appropriate mapper classes in the service layer.</p>
 *
 * <p>For detailed descriptions of each code, see the openFinance API Framework
 * Data Dictionary, section "Status Reason Code".</p>
 *
 * @see SpiGetBulkVopStatusResponse
 * @see SpiVopBulkStatus
 */
public enum SpiStatusReasonCode {

    /**
     * AM04 - Insufficient funds.
     * The debtor's account has insufficient funds to complete the transaction.
     */
    AM04,

    /**
     * AM21 - Invalid party details.
     * The provided party identification details are invalid or incomplete.
     */
    AM21,

    /**
     * BEXX - Backend error.
     * A general backend processing error occurred.
     */
    BEXX,

    /**
     * CN01 - Cancellation requested.
     * The transaction or verification was cancelled as requested.
     */
    CN01,

    /**
     * DS0C - Data format error.
     * The submitted data contains format errors.
     */
    DS0_C,

    /**
     * DS0D - Duplicate submission.
     * The request is a duplicate of a previously submitted request.
     */
    DS0_D,

    /**
     * DS0A - Data validation error.
     * The submitted data failed validation checks.
     */
    DS0_A,

    /**
     * DS04 - Data schema error.
     * The data does not conform to the required schema.
     */
    DS04,

    /**
     * FOCR - Following cancellation request.
     * Action taken following a cancellation request.
     */
    FOCR,

    /**
     * SL11 - Service level agreement error.
     * The request violates service level agreement terms.
     */
    SL11,

    /**
     * TKSP - Technical issue on sender's side.
     * A technical problem occurred on the sender's (TPP) side.
     */
    TKSP,

    /**
     * TKXP - Technical issue on receiver's side.
     * A technical problem occurred on the receiver's (ASPSP) side.
     */
    TKXP,

    /**
     * AM02 - Not allowed due to specific service offering.
     * The operation is not permitted for this specific service configuration.
     */
    AM02,

    /**
     * DT05 - Settlement date/time error.
     * The settlement date or time is invalid or not permitted.
     */
    DT05,

    /**
     * AC05 - Closed account.
     * The specified account is closed and cannot be used.
     */
    AC05,

    /**
     * AC06 - Blocked account.
     * The specified account is blocked and cannot be used.
     */
    AC06,

    /**
     * DS0K - Creditor/debtor mismatch.
     * The creditor and/or debtor information does not match.
     */
    DS0_K,

    /**
     * DT01 - Invalid date.
     * The provided date is invalid or not in the correct format.
     */
    DT01,

    /**
     * UPAY - Unmatched payment.
     * The payment cannot be matched to an expected transaction.
     */
    UPAY,

    /**
     * EOL1 - End of lifecycle.
     * The resource or service has reached its end of life.
     */
    EOL1
}
