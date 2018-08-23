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

package de.adorsys.aspsp.xs2a.web12;

import de.adorsys.aspsp.xs2a.domain.BookingStatus;
import de.adorsys.aspsp.xs2a.domain.ResponseObject;
import de.adorsys.aspsp.xs2a.domain.account.AccountReport;
import de.adorsys.aspsp.xs2a.service.AccountService;
import de.adorsys.aspsp.xs2a.service.mapper.AccountModelMapper;
import de.adorsys.aspsp.xs2a.service.mapper.ResponseMapper;
import de.adorsys.psd2.api.AccountApi;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@AllArgsConstructor
public class AccountController12 implements AccountApi {

    private final AccountService accountService;
    private final ResponseMapper responseMapper;

    @Override
    public ResponseEntity<?> getAccountList(UUID xRequestID, String consentID, Boolean withBalance, String digest, String signature, byte[] tpPSignatureCertificate, String psUIPAddress, Object psUIPPort, String psUAccept, String psUAcceptCharset, String psUAcceptEncoding, String psUAcceptLanguage, String psUUserAgent, String psUHttpMethod, UUID psUDeviceID, String psUGeoLocation) {
        return responseMapper.ok(accountService.getAccountDetailsList(consentID, withBalance), AccountModelMapper::mapToAccountList);
    }

    @Override
    public ResponseEntity<?> readAccountDetails(String accountId, UUID xRequestID, String consentID, Boolean withBalance, String digest, String signature, byte[] tpPSignatureCertificate, String psUIPAddress, Object psUIPPort, String psUAccept, String psUAcceptCharset, String psUAcceptEncoding, String psUAcceptLanguage, String psUUserAgent, String psUHttpMethod, UUID psUDeviceID, String psUGeoLocation) {
        return responseMapper.ok(accountService.getAccountDetails(consentID, accountId, withBalance), AccountModelMapper::mapToAccountDetails);
    }

    @Override
    public ResponseEntity<?> getBalances(String accountId, UUID xRequestID, String consentID, String digest, String signature, byte[] tpPSignatureCertificate, String psUIPAddress, Object psUIPPort, String psUAccept, String psUAcceptCharset, String psUAcceptEncoding, String psUAcceptLanguage, String psUUserAgent, String psUHttpMethod, UUID psUDeviceID, String psUGeoLocation) {
        return responseMapper.ok(accountService.getBalances(consentID, accountId), AccountModelMapper::mapToBalance);
    }

    @Override
    public ResponseEntity<?> getTransactionList(String accountId, String bookingStatus, UUID xRequestID, String consentID, LocalDate dateFrom, LocalDate dateTo, String entryReferenceFrom, Boolean deltaList, Boolean withBalance, String digest, String signature, byte[] tpPSignatureCertificate, String psUIPAddress, Object psUIPPort, String psUAccept, String psUAcceptCharset, String psUAcceptEncoding, String psUAcceptLanguage, String psUUserAgent, String psUHttpMethod, UUID psUDeviceID, String psUGeoLocation) {
        ResponseObject<AccountReport> responseObject =
            accountService.getAccountReport(consentID, accountId, dateFrom, dateTo, null, false, BookingStatus.forValue(bookingStatus), withBalance, deltaList);
        return responseMapper.ok(responseObject, AccountModelMapper::mapToAccountReport);
    }

    @Override
    public ResponseEntity<?> getTransactionDetails(String accountId, String resourceId, UUID xRequestID, String consentID, String digest, String signature, byte[] tpPSignatureCertificate, String psUIPAddress, Object psUIPPort, String psUAccept, String psUAcceptCharset, String psUAcceptEncoding, String psUAcceptLanguage, String psUUserAgent, String psUHttpMethod, UUID psUDeviceID, String psUGeoLocation) {
        /*
        ResponseObject<AccountReport> responseObject =
            accountService.getAccountReport(consentID, accountId, null, null, resourceId, false, null, false, false);
        //TODO need better response for a single transaction
        if (!responseObject.hasError()) {
            TransactionDetails transactionDetails = new TransactionDetails();
            //TODO mapping

            return new ResponseEntity<>(transactionDetails, OK);
        } else {
            return responseMapper.createErrorResponse(responseObject.getError());
        }
        */
        return null;
    }
}
