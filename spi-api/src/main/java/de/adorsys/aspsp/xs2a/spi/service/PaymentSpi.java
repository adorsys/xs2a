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

package de.adorsys.aspsp.xs2a.spi.service;

import de.adorsys.aspsp.xs2a.consent.api.pis.PisPayment;
import de.adorsys.aspsp.xs2a.consent.api.pis.PisPaymentType;
import de.adorsys.aspsp.xs2a.spi.domain.SpiResponse;
import de.adorsys.aspsp.xs2a.spi.domain.authorisation.SpiAuthorisationStatus;
import de.adorsys.aspsp.xs2a.spi.domain.authorisation.SpiScaConfirmation;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiTransactionStatus;
import de.adorsys.aspsp.xs2a.spi.domain.consent.AspspConsentData;
import de.adorsys.aspsp.xs2a.spi.domain.payment.*;
import de.adorsys.aspsp.xs2a.spi.domain.authorisation.SpiScaMethod;

import java.util.List;

public interface PaymentSpi extends AuthorisationSpi<SpiPayment> {
    /**
     * Initiates a single payment at ASPSP
     *
     * @param spiSinglePayment single payment to be sent for saving at ASPSP
     * @param aspspConsentData Encrypted data that may stored in the consent management system in the consent linked to a request.
     *                         May be null if consent does not contain such data, or request isn't done from a workflow with a consent
     * @return Response from ASPSP containing information about carried payment initiation operation
     * @deprecated since 1.8. Will be removed in 1.9. Use {@link #initiatePayment(SpiPayment, AspspConsentData)}
     */
    @Deprecated
    SpiResponse<SpiPaymentInitialisationResponse> createPaymentInitiation(SpiSinglePayment spiSinglePayment, AspspConsentData aspspConsentData);

    /**
     * Initiates a periodic payment at ASPSP
     *
     * @param periodicPayment  periodic payment to be sent for saving at ASPSP
     * @param aspspConsentData Encrypted data that may stored in the consent management system in the consent linked to a request.
     *                         May be null if consent does not contain such data, or request isn't done from a workflow with a consent
     * @return Response from ASPSP containing information about carried payment initiation operation
     * @deprecated since 1.8. Will be removed in 1.9. Use {@link #initiatePayment(SpiPayment, AspspConsentData)}
     */
    @Deprecated
    SpiResponse<SpiPaymentInitialisationResponse> initiatePeriodicPayment(SpiPeriodicPayment periodicPayment, AspspConsentData aspspConsentData);

    /**
     * Initiates a bulk payment at ASPSP
     *
     * @param spiBulkPayment   bulk payment to be sent for saving at ASPSP
     * @param aspspConsentData Encrypted data that may stored in the consent management system in the consent linked to a request.
     *                         May be null if consent does not contain such data, or request isn't done from a workflow with a consent
     * @return Response from ASPSP containing information about carried payment initiation operation
     * @deprecated since 1.8. Will be removed in 1.9. Use {@link #initiatePayment(SpiPayment, AspspConsentData)}
     */
    @Deprecated
    SpiResponse<List<SpiPaymentInitialisationResponse>> createBulkPayments(SpiBulkPayment spiBulkPayment, AspspConsentData aspspConsentData);

    /**
     * Returns a payment status by its ASPSP identifier
     *
     * @param paymentId        ASPSP identifier of a payment
     * @param paymentType      Type of payment
     * @param aspspConsentData Encrypted data that may stored in the consent management system in the consent linked to a request.
     *                         May be null if consent does not contain such data, or request isn't done from a workflow with a consent
     * @return payment status
     * @deprecated since 1.8. Will be removed in 1.9. Use {@link #getPaymentStatusById(String, SpiPayment, AspspConsentData)}
     */
    @Deprecated
    SpiResponse<SpiTransactionStatus> getPaymentStatusById(String paymentId, SpiPaymentType paymentType, AspspConsentData aspspConsentData);

    /**
     * Returns a single payment by its ASPSP identifier
     *
     * @param paymentType      Type of payment
     * @param paymentProduct   The addressed payment product
     * @param paymentId        ASPSP identifier of a payment
     * @param aspspConsentData Encrypted data that may stored in the consent management system in the consent linked to a request.
     *                         May be null if consent does not contain such data, or request isn't done from a workflow with a consent
     * @return single payment
     * @deprecated since 1.8. Will be removed in 1.9. Use {@link #getPaymentById(SpiPayment, String, AspspConsentData)}
     */
    @Deprecated
    SpiResponse<SpiSinglePayment> getSinglePaymentById(SpiPaymentType paymentType, String paymentProduct, String paymentId, AspspConsentData aspspConsentData);

    /**
     * Returns a periodic payment by its ASPSP identifier
     *
     * @param paymentType      Type of payment
     * @param paymentProduct   The addressed payment product
     * @param paymentId        ASPSP identifier of a payment
     * @param aspspConsentData Encrypted data that may stored in the consent management system in the consent linked to a request.
     *                         May be null if consent does not contain such data, or request isn't done from a workflow with a consent
     * @return periodic payment
     * @deprecated since 1.8. Will be removed in 1.9. Use {@link #getPaymentById(SpiPayment, String, AspspConsentData)}
     */
    @Deprecated
    SpiResponse<SpiPeriodicPayment> getPeriodicPaymentById(SpiPaymentType paymentType, String paymentProduct, String paymentId, AspspConsentData aspspConsentData);

    /**
     * Returns a bulk payment by its ASPSP identifier
     *
     * @param paymentType      Type of payment
     * @param paymentProduct   The addressed payment product
     * @param paymentId        ASPSP identifier of a payment
     * @param aspspConsentData Encrypted data that may stored in the consent management system in the consent linked to a request.
     *                         May be null if consent does not contain such data, or request isn't done from a workflow with a consent
     * @return bulk payment
     * @deprecated since 1.8. Will be removed in 1.9. Use {@link #getPaymentById(SpiPayment, String, AspspConsentData)}
     */
    @Deprecated
    SpiResponse<List<SpiSinglePayment>> getBulkPaymentById(SpiPaymentType paymentType, String paymentProduct, String paymentId, AspspConsentData aspspConsentData);

    /**
     * Authorises psu and returns current autorization status
     *
     * @param psuId            ASPSP identifier of the psu
     * @param password         Psu's password
     * @param aspspConsentData Encrypted data that may stored in the consent management system in the consent linked to a request.
     *                         May be null if consent does not contain such data, or request isn't done from a workflow with a consent
     * @return success or failure authorization status
     * @deprecated since 1.8. Will be removed in 1.9.
     */
    @Deprecated
    SpiResponse<SpiAuthorisationStatus> authorisePsu(String psuId, String password, AspspConsentData aspspConsentData);

    /**
     * Returns a list of SCA methods for PSU by its login
     *
     * @param psuId            ASPSP identifier of the psu
     * @param aspspConsentData Encrypted data that may stored in the consent management system in the consent linked to a request.
     *                         May be null if consent does not contain such data, or request isn't done from a workflow with a consent
     * @return a list of SCA methods applicable for specified PSU
     * @deprecated since 1.8. Will be removed in 1.9.
     */
    @Deprecated
    SpiResponse<List<SpiScaMethod>> readAvailableScaMethod(String psuId, AspspConsentData aspspConsentData);

    /**
     * Returns a bulk payment by its ASPSP identifier
     *
     * @param pisPaymentType   Type of payment
     * @param pisPayments      List of payments for execution
     * @param aspspConsentData Encrypted data that may stored in the consent management system in the consent linked to a request.
     *                         May be null if consent does not contain such data, or request isn't done from a workflow with a consent
     * @return execution payment id
     * @deprecated since 1.8. Will be removed in 1.9. Use {@link #executePaymentWithoutSca(SpiPaymentType, SpiPayment, AspspConsentData)}
     */
    @Deprecated
    SpiResponse<String> executePayment(PisPaymentType pisPaymentType, List<PisPayment> pisPayments, AspspConsentData aspspConsentData);

    /**
     * Performs strong customer authorization
     *
     * @param psuId            ASPSP identifier of the psu
     * @param aspspConsentData Encrypted data that may stored in the consent management system in the consent linked to a request.
     *                         May be null if consent does not contain such data, or request isn't done from a workflow with a consent
     * @deprecated since 1.8. Will be removed in 1.9.
     */
    @Deprecated
    void performStrongUserAuthorisation(String psuId, AspspConsentData aspspConsentData);

    /**
     * @deprecated since 1.8. Will be removed in 1.9.
     */
    @Deprecated
    void applyStrongUserAuthorisation(SpiScaConfirmation spiScaConfirmation, AspspConsentData aspspConsentData);

    /**
     * Initiates a payment at ASPSP. SPI Implementation shall return paymentId here. Used in all SCA approaches.
     *
     * @param spiPayment       payment to be sent for saving at ASPSP
     * @param aspspConsentData Encrypted data that may stored in the consent management system in the consent linked to a request.
     *                         May be null if consent does not contain such data, or request isn't done from a workflow with a consent
     * @return Response from ASPSP containing information about carried payment initiation operation
     */
    SpiResponse<SpiPaymentInitialisationResponse> initiatePayment(SpiPayment spiPayment, AspspConsentData aspspConsentData);

    /**
     * Performs payment execution at ASPSP side when no SCA method are set for PSU. Used only with embedded SCA Approach.
     *
     * @param spiPaymentType   Type of payment
     * @param spiPayment       generic payment for execution
     * @param aspspConsentData Encrypted data that may stored in the consent management system in the consent linked to a request.
     *                         May be null if consent does not contain such data, or request isn't done from a workflow with a consent
     * @return execution payment id
     */
    SpiResponse executePaymentWithoutSca(SpiPaymentType spiPaymentType, SpiPayment spiPayment, AspspConsentData aspspConsentData);

    /**
     * Returns a payment by its ASPSP identifier. Used in all SCA Approaches.
     *
     * @param spiPayment       generic payment object
     * @param paymentId        ASPSP identifier of a payment
     * @param aspspConsentData Encrypted data that may stored in the consent management system in the consent linked to a request.
     *                         May be null if consent does not contain such data, or request isn't done from a workflow with a consent
     * @return bulk payment
     */
    SpiResponse<SpiPayment> getPaymentById(SpiPayment spiPayment, String paymentId, AspspConsentData aspspConsentData);

    /**
     * Returns a payment status by its ASPSP identifier. Used in all SCA Approaches.
     *
     * @param paymentId        ASPSP identifier of a payment
     * @param spiPayment       generic payment object
     * @param aspspConsentData Encrypted data that may stored in the consent management system in the consent linked to a request.
     *                         May be null if consent does not contain such data, or request isn't done from a workflow with a consent
     * @return payment status
     */
    SpiResponse<SpiTransactionStatus> getPaymentStatusById(String paymentId, SpiPayment spiPayment, AspspConsentData aspspConsentData);

}
