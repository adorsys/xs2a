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
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiTransactionStatus;
import de.adorsys.aspsp.xs2a.spi.domain.consent.AspspConsentData;
import de.adorsys.aspsp.xs2a.spi.domain.payment.*;
import de.adorsys.aspsp.xs2a.spi.domain.psu.SpiScaMethod;

import java.util.List;

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
     * @param payments         bulk payment to be sent for saving at ASPSP
     * @param aspspConsentData Encrypted data that may stored in the consent management system in the consent linked to a request.
     *                         May be null if consent does not contain such data, or request isn't done from a workflow with a consent
     * @return Response from ASPSP containing information about carried payment initiation operation
     */
    SpiResponse<List<SpiPaymentInitialisationResponse>> createBulkPayments(List<SpiSinglePayment> payments, AspspConsentData aspspConsentData);

    /**
     * Returns a payment status by its ASPSP identifier
     *
     * @param paymentId        ASPSP identifier of a payment
     * @param paymentType      Type of payment
     * @param aspspConsentData Encrypted data that may stored in the consent management system in the consent linked to a request.
     *                         May be null if consent does not contain such data, or request isn't done from a workflow with a consent
     * @return payment status
     */
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
     */
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
     */
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
     */
    SpiResponse<List<SpiSinglePayment>> getBulkPaymentById(SpiPaymentType paymentType, String paymentProduct, String paymentId, AspspConsentData aspspConsentData);

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
     * @param aspspConsentData Encrypted data that may stored in the consent management system in the consent linked to a request.
     *                         May be null if consent does not contain such data, or request isn't done from a workflow with a consent
     * @return a list of SCA methods applicable for specified PSU
     */
    SpiResponse<List<SpiScaMethod>> readAvailableScaMethod(AspspConsentData aspspConsentData);

    /**
     * Returns a bulk payment by its ASPSP identifier
     *
     * @param pisPaymentType   Type of payment
     * @param pisPayments      List of payments for execution
     * @param aspspConsentData Encrypted data that may stored in the consent management system in the consent linked to a request.
     *                         May be null if consent does not contain such data, or request isn't done from a workflow with a consent
     * @return execution payment id
     */
    SpiResponse<String> executePayment(PisPaymentType pisPaymentType, List<PisPayment> pisPayments, AspspConsentData aspspConsentData);

    /**
     * Performs strong customer authorization
     *
     * @param aspspConsentData Encrypted data that may stored in the consent management system in the consent linked to a request.
     *                         May be null if consent does not contain such data, or request isn't done from a workflow with a consent
     */
    void performStrongUserAuthorisation(AspspConsentData aspspConsentData);

    void applyStrongUserAuthorisation(SpiPaymentConfirmation spiPaymentConfirmation, AspspConsentData aspspConsentData);
}
