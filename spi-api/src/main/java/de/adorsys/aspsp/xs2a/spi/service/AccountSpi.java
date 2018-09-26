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

import de.adorsys.aspsp.xs2a.spi.domain.SpiResponse;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountConfirmation;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountDetails;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiTransaction;
import de.adorsys.aspsp.xs2a.spi.domain.authorisation.SpiAuthorisationStatus;
import de.adorsys.aspsp.xs2a.spi.domain.consent.AspspConsentData;
import de.adorsys.aspsp.xs2a.spi.domain.psu.SpiScaMethod;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AccountSpi {
    /**
     * Queries ASPSP to get List of transactions dependant on period and accountId
     *
     * @param accountId        String representation of ASPSP account primary identifier
     * @param dateFrom         Date representing the beginning of the search period
     * @param dateTo           Date representing the ending of the search period
     * @param aspspConsentData Encrypted data that may be stored in the consent management system in the consent linked to a request.<br>
     *                         May be null if consent does not contain such data, or request isn't done from a workflow with a consent
     * @return List of transactions
     */
    @Deprecated
    SpiResponse<List<SpiTransaction>> readTransactionsByPeriod(String accountId, LocalDate dateFrom, LocalDate dateTo, AspspConsentData aspspConsentData);

    /**
     * Queries ASPSP to (GET) transaction by its primary identifier and account identifier
     *
     * @param transactionId    String representation of ASPSP primary identifier of transaction
     * @param accountId        String representation of ASPSP account primary identifier
     * @param aspspConsentData Encrypted data that may be stored in the consent management system in the consent linked to a request.<br>
     *                         May be null if consent does not contain such data, or request isn't done from a workflow with a consent
     * @return Transaction
     */
    @Deprecated
    SpiResponse<Optional<SpiTransaction>> readTransactionById(String transactionId, String accountId, AspspConsentData aspspConsentData);

    /**
     * Queries ASPSP to (GET) AccountDetails by primary ASPSP account identifier
     *
     * @param accountId        String representation of ASPSP account primary identifier
     * @param aspspConsentData Encrypted data that may be stored in the consent management system in the consent linked to a request.<br>
     *                         May be null if consent does not contain such data, or request isn't done from a workflow with a consent
     * @return Account details
     */
    @Deprecated
    SpiResponse<SpiAccountDetails> readAccountDetails(String accountId, AspspConsentData aspspConsentData);

    /**
     * Queries ASPSP to (GET) a list of account details of a certain PSU by identifier
     *
     * @param psuId            String representing ASPSP`s primary identifier of PSU
     * @param aspspConsentData Encrypted data that may be stored in the consent management system in the consent linked to a request.<br>
     *                         May be null if consent does not contain such data, or request isn't done from a workflow with a consent
     * @return List of account details
     */
    @Deprecated
    SpiResponse<List<SpiAccountDetails>> readAccountsByPsuId(String psuId, AspspConsentData aspspConsentData);

    /**
     * Queries ASPSP to (GET) List of AccountDetails by IBAN
     *
     * @param iban             String representation of Account IBAN
     * @param aspspConsentData Encrypted data that may be stored in the consent management system in the consent linked to a request.<br>
     *                         May be null if consent does not contain such data, or request isn't done from a workflow with a consent
     * @return List of account details
     */
    @Deprecated
    SpiResponse<List<SpiAccountDetails>> readAccountDetailsByIban(String iban, AspspConsentData aspspConsentData);

    /**
     * Queries ASPSP to (GET) list of account details with certain account IBANS
     *
     * @param ibans            a collection of Strings representing account IBANS
     * @param aspspConsentData Encrypted data that may be stored in the consent management system in the consent linked to a request.<br>
     *                         May be null if consent does not contain such data, or request isn't done from a workflow with a consent
     * @return List of account details
     */
    @Deprecated
    SpiResponse<List<SpiAccountDetails>> readAccountDetailsByIbans(Collection<String> ibans, AspspConsentData aspspConsentData);

    /**
     * Queries ASPSP to (GET) list of allowed payment products for current PSU by its account reference
     *
     * @param reference        Account reference
     * @param aspspConsentData Encrypted data that may be stored in the consent management system in the consent linked to a request.<br>
     *                         May be null if consent does not contain such data, or request isn't done from a workflow with a consent
     * @return a list of allowed payment products
     */
    @Deprecated
    SpiResponse<List<String>> readPsuAllowedPaymentProductList(SpiAccountReference reference, AspspConsentData aspspConsentData);

    @Deprecated
    SpiResponse<List<SpiScaMethod>> readAvailableScaMethods(String psuId, String password);

    /**
     * Authorises psu and returns current autorization status
     *
     * @param psuId            ASPSP identifier of the psu
     * @param password         Psu's password
     * @param aspspConsentData Encrypted data that may be stored in the consent management system in the consent linked to a request.
     *                         May be null if consent does not contain such data, or request isn't done from a workflow with a consent
     * @return success or failure authorization status
     */
    @Deprecated
    SpiResponse<SpiAuthorisationStatus> authorisePsu(String psuId, String password, AspspConsentData aspspConsentData);

    /**
     * Performs strong customer authorization
     *
     * @param psuId            ASPSP identifier of the psu
     * @param aspspConsentData Encrypted data that may be stored in the consent management system in the consent linked to a request.
     *                         May be null if consent does not contain such data, or request isn't done from a workflow with a consent
     */
    @Deprecated
    void performStrongUserAuthorisation(String psuId, AspspConsentData aspspConsentData);

    /**
     * Applies strong customer authorization
     *
     * @param spiAccountConfirmation Account confirmation data
     * @param aspspConsentData       Encrypted data that may be stored in the consent management system in the consent linked to a request.
     *                               May be null if consent does not contain such data, or request isn't done from a workflow with a consent
     */
    @Deprecated
    void applyStrongUserAuthorisation(SpiAccountConfirmation spiAccountConfirmation, AspspConsentData aspspConsentData);
}
