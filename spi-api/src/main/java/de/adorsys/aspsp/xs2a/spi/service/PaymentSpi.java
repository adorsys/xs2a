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

import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiBulkPayment;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPaymentInitialisationResponse;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPeriodicPayment;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayment;
import de.adorsys.psd2.consent.api.pis.PisPayment;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthorisationStatus;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiScaConfirmation;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiScaMethod;
import de.adorsys.psd2.xs2a.spi.domain.common.SpiTransactionStatus;
import de.adorsys.psd2.xs2a.spi.domain.consent.AspspConsentData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;

import java.util.List;

/**
 * @deprecated since 1.8. Will be removed in 1.10
 */
@Deprecated
public interface PaymentSpi {
    /**
     * Initiates a single payment at ASPSP
     *
     * @param spiSinglePayment single payment to be sent for saving at ASPSP
     * @param aspspConsentData Encrypted data that may stored in the consent management system in the consent linked to a request.
     *                         May be null if consent does not contain such data, or request isn't done from a workflow with a consent
     * @return Response from ASPSP containing information about carried payment initiation operation
     */
    SpiResponse<SpiPaymentInitialisationResponse> createPaymentInitiation(SpiSinglePayment spiSinglePayment, AspspConsentData aspspConsentData);

    /**
     * Initiates a periodic payment at ASPSP
     *
     * @param periodicPayment  periodic payment to be sent for saving at ASPSP
     * @param aspspConsentData Encrypted data that may stored in the consent management system in the consent linked to a request.
     *                         May be null if consent does not contain such data, or request isn't done from a workflow with a consent
     * @return Response from ASPSP containing information about carried payment initiation operation
     */
    SpiResponse<SpiPaymentInitialisationResponse> initiatePeriodicPayment(SpiPeriodicPayment periodicPayment, AspspConsentData aspspConsentData);

    /**
     * Initiates a bulk payment at ASPSP
     *
     * @param spiBulkPayment   bulk payment to be sent for saving at ASPSP
     * @param aspspConsentData Encrypted data that may stored in the consent management system in the consent linked to a request.
     *                         May be null if consent does not contain such data, or request isn't done from a workflow with a consent
     * @return Response from ASPSP containing information about carried payment initiation operation
     */
    SpiResponse<List<SpiPaymentInitialisationResponse>> createBulkPayments(SpiBulkPayment spiBulkPayment, AspspConsentData aspspConsentData);

    /**
     * Returns a payment status by its ASPSP identifier
     *
     * @param paymentId        ASPSP identifier of a payment
     * @param paymentType      Type of payment
     * @param aspspConsentData Encrypted data that may stored in the consent management system in the consent linked to a request.
     *                         May be null if consent does not contain such data, or request isn't done from a workflow with a consent
     * @return payment status
     */
    SpiResponse<SpiTransactionStatus> getPaymentStatusById(String paymentId, PaymentType paymentType, AspspConsentData aspspConsentData);

    /**
     * Returns a single payment by its ASPSP identifier
     *
     * @param paymentType      Type of payment
     * @param paymentProduct   The addressed payment product
     * @param paymentId        ASPSP identifier of a payment
     * @param aspspConsentData Encrypted data that may stored in the consent management system in the consent linked to a request.
     *                         May be null if consent does not contain such data, or request isn't done from a workflow with a consent
     * @return single payment
     */
    SpiResponse<SpiSinglePayment> getSinglePaymentById(PaymentType paymentType, String paymentProduct, String paymentId, AspspConsentData aspspConsentData);

    /**
     * Returns a periodic payment by its ASPSP identifier
     *
     * @param paymentType      Type of payment
     * @param paymentProduct   The addressed payment product
     * @param paymentId        ASPSP identifier of a payment
     * @param aspspConsentData Encrypted data that may stored in the consent management system in the consent linked to a request.
     *                         May be null if consent does not contain such data, or request isn't done from a workflow with a consent
     * @return periodic payment
     */
    SpiResponse<SpiPeriodicPayment> getPeriodicPaymentById(PaymentType paymentType, String paymentProduct, String paymentId, AspspConsentData aspspConsentData);

    /**
     * Returns a bulk payment by its ASPSP identifier
     *
     * @param paymentType      Type of payment
     * @param paymentProduct   The addressed payment product
     * @param paymentId        ASPSP identifier of a payment
     * @param aspspConsentData Encrypted data that may stored in the consent management system in the consent linked to a request.
     *                         May be null if consent does not contain such data, or request isn't done from a workflow with a consent
     * @return bulk payment
     */
    SpiResponse<List<SpiSinglePayment>> getBulkPaymentById(PaymentType paymentType, String paymentProduct, String paymentId, AspspConsentData aspspConsentData);

    /**
     * Authorises psu and returns current autorization status
     *
     * @param psuId            ASPSP identifier of the psu
     * @param password         Psu's password
     * @param aspspConsentData Encrypted data that may stored in the consent management system in the consent linked to a request.
     *                         May be null if consent does not contain such data, or request isn't done from a workflow with a consent
     * @return success or failure authorization status
     */
    SpiResponse<SpiAuthorisationStatus> authorisePsu(String psuId, String password, AspspConsentData aspspConsentData);

    /**
     * Returns a list of SCA methods for PSU by its login
     *
     * @param psuId            ASPSP identifier of the psu
     * @param aspspConsentData Encrypted data that may stored in the consent management system in the consent linked to a request.
     *                         May be null if consent does not contain such data, or request isn't done from a workflow with a consent
     * @return a list of SCA methods applicable for specified PSU
     */
    SpiResponse<List<SpiScaMethod>> readAvailableScaMethod(String psuId, AspspConsentData aspspConsentData);

    /**
     * Returns a bulk payment by its ASPSP identifier
     *
     * @param pisPaymentType   Type of payment
     * @param pisPayments      List of payments for execution
     * @param aspspConsentData Encrypted data that may stored in the consent management system in the consent linked to a request.
     *                         May be null if consent does not contain such data, or request isn't done from a workflow with a consent
     * @return execution payment id
     */
    SpiResponse<String> executePayment(PaymentType pisPaymentType, List<PisPayment> pisPayments, AspspConsentData aspspConsentData);

    /**
     * Performs strong customer authorization
     *
     * @param psuId            ASPSP identifier of the psu
     * @param choosenMethod     Chosen SCA Method
     * @param aspspConsentData Encrypted data that may stored in the consent management system in the consent linked to a request.
     *                         May be null if consent does not contain such data, or request isn't done from a workflow with a consent
     * @return Void response with execution status
     */
    SpiResponse<Void> performStrongUserAuthorisation(String psuId, SpiScaMethod choosenMethod, AspspConsentData aspspConsentData);

    SpiResponse<Void> applyStrongUserAuthorisation(SpiScaConfirmation spiScaConfirmation, AspspConsentData aspspConsentData);
}
