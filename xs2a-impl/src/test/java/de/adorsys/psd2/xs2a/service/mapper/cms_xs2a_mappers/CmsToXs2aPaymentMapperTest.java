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

package de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers;

import de.adorsys.psd2.consent.api.CmsAddress;
import de.adorsys.psd2.consent.api.pis.CmsRemittance;
import de.adorsys.psd2.consent.api.pis.PisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.pis.PisPayment;
import de.adorsys.psd2.xs2a.core.pis.PisDayOfExecution;
import de.adorsys.psd2.xs2a.core.pis.PisExecutionRule;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.profile.AccountReferenceType;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.pis.CommonPayment;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Currency;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CmsToXs2aPaymentMapperTest {
    private static final String PAYMENT_ID = "payment id";
    private static final String EXECUTION_ID = "execution id";
    private static final String END_TO_END_IDENTIFICATION = "RI-123456789";
    private static final String INSTRUCTION_IDENTIFICATION = "INSTRUCTION_IDENTIFICATION";
    private static final Currency CURRENCY = Currency.getInstance("EUR");

    private static final String DEBTOR_IBAN = "debtor iban";
    private static final AccountReference DEBTOR_ACCOUNT_REFERENCE = new AccountReference(AccountReferenceType.IBAN,
                                                                                          DEBTOR_IBAN,
                                                                                          CURRENCY);

    private static final String ULTIMATE_DEBTOR = "ultimate debtor";
    private static final String AMOUNT = "100";

    private static final String CREDITOR_IBAN = "creditor iban";
    private static final AccountReference CREDITOR_ACCOUNT_REFERENCE = new AccountReference(AccountReferenceType.IBAN,
                                                                                            CREDITOR_IBAN,
                                                                                            CURRENCY);

    private static final String CREDITOR_AGENT = "creditor agent";
    private static final String CREDITOR_NAME = "creditor name";

    private static final String CREDITOR_ADDRESS_STREET_NAME = "street name";
    private static final String CREDITOR_ADDRESS_BUILDING_NUMBER = "building number";
    private static final String CREDITOR_ADDRESS_TOWN_NAME = "town name";
    private static final String CREDITOR_ADDRESS_POST_CODE = "post code";
    private static final String CREDITOR_ADDRESS_COUNTRY = "DE";

    private static final String REMITTANCE_INFORMATION_UNSTRUCTURED = "remittance information unstructured";
    private static final String REMITTANCE_INFORMATION_STRUCTURED_REFERENCE = "structured reference";
    private static final LocalDate REQUESTED_EXECUTION_DATE = LocalDate.of(2019, 2, 27);
    private static final OffsetDateTime REQUESTED_EXECUTION_TIME = OffsetDateTime.of(REQUESTED_EXECUTION_DATE,
                                                                                     LocalTime.NOON,
                                                                                     ZoneOffset.UTC);
    private static final OffsetDateTime STATUS_CHANGE_TIMESTAMP = OffsetDateTime.of(REQUESTED_EXECUTION_DATE,
                                                                                    LocalTime.NOON,
                                                                                    ZoneOffset.UTC);

    private static final String ULTIMATE_CREDITOR = "ultimate creditor";
    private static final String PURPOSE_CODE = "BKDF";
    private static final TransactionStatus TRANSACTION_STATUS = TransactionStatus.RCVD;
    private static final LocalDate START_DATE = LocalDate.of(2019, 2, 25);
    private static final LocalDate END_DATE = LocalDate.of(2019, 2, 28);
    private static final PisExecutionRule EXECUTION_RULE = PisExecutionRule.FOLLOWING;
    private static final String FREQUENCY = "ANNUAL";
    private static final PisDayOfExecution DAY_OF_EXECUTION = PisDayOfExecution.DAY_2;
    private static final List<PsuIdData> PSU_ID_DATA_LIST = Collections.singletonList(new PsuIdData("psu id", null, null, null, null));

    private static final String PAYMENT_PRODUCT = "payment product";
    private static final PaymentType PAYMENT_TYPE = PaymentType.SINGLE;
    private static final TppInfo TPP_INFO = buildTppInfo();
    private static final byte[] PAYMENT_DATA = "payment data".getBytes();


    private CmsToXs2aPaymentMapper cmsToXs2aPaymentMapper = new CmsToXs2aPaymentMapper();

    @Test
    void mapToXs2aCommonPayment() {
        PisCommonPaymentResponse pisCommonPaymentResponse = buildPisCommonPaymentResponse();

        CommonPayment commonPayment = cmsToXs2aPaymentMapper.mapToXs2aCommonPayment(pisCommonPaymentResponse);

        assertNotNull(commonPayment);
        assertEquals(PAYMENT_ID, commonPayment.getPaymentId());
        assertEquals(PAYMENT_PRODUCT, commonPayment.getPaymentProduct());
        assertEquals(TRANSACTION_STATUS, commonPayment.getTransactionStatus());
        assertEquals(PAYMENT_TYPE, commonPayment.getPaymentType());
        assertEquals(PAYMENT_DATA, commonPayment.getPaymentData());
        assertEquals(PSU_ID_DATA_LIST, commonPayment.getPsuDataList());
        assertEquals(STATUS_CHANGE_TIMESTAMP, commonPayment.getStatusChangeTimestamp());
        assertEquals(pisCommonPaymentResponse.getCreationTimestamp(), commonPayment.getCreationTimestamp());
    }

    @Test
    void mapToXs2aCommonPayment_withNullPisCommonPaymentResponse_shouldReturnNull() {
        CommonPayment commonPayment = cmsToXs2aPaymentMapper.mapToXs2aCommonPayment(null);

        assertNull(commonPayment);
    }

    private static TppInfo buildTppInfo() {
        TppInfo tppInfo = new TppInfo();
        tppInfo.setAuthorisationNumber("authorisation number");
        tppInfo.setAuthorityId("authority id");
        return tppInfo;
    }

    private PisPayment buildPisPayment() {
        PisPayment pisPayment = new PisPayment();
        pisPayment.setPaymentId(PAYMENT_ID);
        pisPayment.setExecutionId(EXECUTION_ID);
        pisPayment.setEndToEndIdentification(END_TO_END_IDENTIFICATION);
        pisPayment.setInstructionIdentification(INSTRUCTION_IDENTIFICATION);
        pisPayment.setDebtorAccount(DEBTOR_ACCOUNT_REFERENCE);
        pisPayment.setUltimateDebtor(ULTIMATE_DEBTOR);
        pisPayment.setCurrency(CURRENCY);
        pisPayment.setAmount(new BigDecimal(AMOUNT));
        pisPayment.setCreditorAccount(CREDITOR_ACCOUNT_REFERENCE);
        pisPayment.setCreditorAgent(CREDITOR_AGENT);
        pisPayment.setCreditorName(CREDITOR_NAME);
        pisPayment.setCreditorAddress(buildCmsAddress());
        pisPayment.setRemittanceInformationUnstructured(REMITTANCE_INFORMATION_UNSTRUCTURED);
        pisPayment.setRemittanceInformationStructured(buildCmsRemittance());
        pisPayment.setRequestedExecutionDate(REQUESTED_EXECUTION_DATE);
        pisPayment.setRequestedExecutionTime(REQUESTED_EXECUTION_TIME);
        pisPayment.setUltimateCreditor(ULTIMATE_CREDITOR);
        pisPayment.setPurposeCode(PURPOSE_CODE);
        pisPayment.setTransactionStatus(TRANSACTION_STATUS);
        pisPayment.setStartDate(START_DATE);
        pisPayment.setEndDate(END_DATE);
        pisPayment.setExecutionRule(EXECUTION_RULE);
        pisPayment.setFrequency(FREQUENCY);
        pisPayment.setDayOfExecution(DAY_OF_EXECUTION);
        pisPayment.setPsuDataList(PSU_ID_DATA_LIST);
        pisPayment.setStatusChangeTimestamp(STATUS_CHANGE_TIMESTAMP);
        pisPayment.setBatchBookingPreferred(Boolean.TRUE);
        pisPayment.setCreationTimestamp(OffsetDateTime.now());
        return pisPayment;
    }

    private CmsAddress buildCmsAddress() {
        CmsAddress cmsAddress = new CmsAddress();
        cmsAddress.setStreetName(CREDITOR_ADDRESS_STREET_NAME);
        cmsAddress.setBuildingNumber(CREDITOR_ADDRESS_BUILDING_NUMBER);
        cmsAddress.setTownName(CREDITOR_ADDRESS_TOWN_NAME);
        cmsAddress.setPostCode(CREDITOR_ADDRESS_POST_CODE);
        cmsAddress.setCountry(CREDITOR_ADDRESS_COUNTRY);
        return cmsAddress;
    }

    private CmsRemittance buildCmsRemittance() {
        CmsRemittance cmsRemittance = new CmsRemittance();
        cmsRemittance.setReference(REMITTANCE_INFORMATION_STRUCTURED_REFERENCE);
        return cmsRemittance;
    }

    private PisCommonPaymentResponse buildPisCommonPaymentResponse() {
        PisCommonPaymentResponse pisCommonPaymentResponse = new PisCommonPaymentResponse();

        pisCommonPaymentResponse.setPayments(Collections.singletonList(buildPisPayment()));
        pisCommonPaymentResponse.setPaymentProduct(PAYMENT_PRODUCT);
        pisCommonPaymentResponse.setPaymentType(PAYMENT_TYPE);
        pisCommonPaymentResponse.setTppInfo(TPP_INFO);
        pisCommonPaymentResponse.setExternalId(PAYMENT_ID);
        pisCommonPaymentResponse.setPsuData(PSU_ID_DATA_LIST);
        pisCommonPaymentResponse.setPaymentData(PAYMENT_DATA);
        pisCommonPaymentResponse.setTransactionStatus(TRANSACTION_STATUS);
        pisCommonPaymentResponse.setStatusChangeTimestamp(STATUS_CHANGE_TIMESTAMP);
        pisCommonPaymentResponse.setMultilevelScaRequired(false);
        pisCommonPaymentResponse.setCreationTimestamp(OffsetDateTime.now());

        return pisCommonPaymentResponse;
    }
}
