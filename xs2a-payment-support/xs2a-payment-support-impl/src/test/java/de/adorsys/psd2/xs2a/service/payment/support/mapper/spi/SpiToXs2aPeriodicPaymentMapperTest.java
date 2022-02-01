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

package de.adorsys.psd2.xs2a.service.payment.support.mapper.spi;

import de.adorsys.psd2.core.payment.model.PurposeCode;
import de.adorsys.psd2.xs2a.core.pis.FrequencyCode;
import de.adorsys.psd2.xs2a.core.pis.PisDayOfExecution;
import de.adorsys.psd2.xs2a.core.pis.PisExecutionRule;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.domain.pis.PeriodicPayment;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aAccountReferenceMapperImpl;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aAddressMapperImpl;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aAmountMapperImpl;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.psd2.xs2a.spi.domain.common.SpiAmount;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiAddress;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiPeriodicPayment;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.web.mapper.RemittanceMapperImpl;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Currency;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
    classes = {SpiToXs2aPeriodicPaymentMapperImpl.class, SpiToXs2aAmountMapperImpl.class,
        SpiToXs2aAddressMapperImpl.class, SpiToXs2aAccountReferenceMapperImpl.class, RemittanceMapperImpl.class})
class SpiToXs2aPeriodicPaymentMapperTest {
    private static final String PAYMENT_PRODUCT = "sepa-credit-transfers";
    private static final String PAYMENT_ID = "2Cixxv85Or_qoBBh_d7VTZC0M8PwzR5IGzsJuT-jYHNOMR1D7n69vIF46RgFd7Zn_=_bS6p6XvTWI";
    private static final OffsetDateTime OFFSET_DATE_TIME = OffsetDateTime.now();

    @Autowired
    private SpiToXs2aPeriodicPaymentMapper mapper;

    private final JsonReader jsonReader = new JsonReader();

    @Test
    void mapToXs2aPeriodicPayment() {
        PeriodicPayment periodicPayment = mapper.mapToXs2aPeriodicPayment(buildSpiPeriodicPayment());

        PeriodicPayment expectedPeriodicPayment = jsonReader.getObjectFromFile("json/support/mapper/xs2a-periodic-payment.json", PeriodicPayment.class);
        expectedPeriodicPayment.setRequestedExecutionDate(OFFSET_DATE_TIME.toLocalDate());
        expectedPeriodicPayment.setRequestedExecutionTime(OFFSET_DATE_TIME);
        expectedPeriodicPayment.setStatusChangeTimestamp(OFFSET_DATE_TIME);
        assertEquals(expectedPeriodicPayment, periodicPayment);
    }

    @Test
    void mapToXs2aPeriodicPayment_nullValue() {
        PeriodicPayment periodicPayment = mapper.mapToXs2aPeriodicPayment(null);
        assertNull(periodicPayment);
    }

    private SpiPeriodicPayment buildSpiPeriodicPayment() {
        SpiPeriodicPayment payment = new SpiPeriodicPayment(PAYMENT_PRODUCT);
        payment.setPaymentId(PAYMENT_ID);
        payment.setDayOfExecution(PisDayOfExecution.DAY_14);
        payment.setStartDate(LocalDate.of(2017, 3, 3));
        payment.setEndDate(LocalDate.of(2020, 12, 2));
        payment.setExecutionRule(PisExecutionRule.PRECEDING);
        payment.setFrequency(FrequencyCode.ANNUAL);
        payment.setPaymentStatus(TransactionStatus.ACCP);
        SpiAccountReference accountReferenceCreditor = jsonReader.getObjectFromFile("json/support/mapper/spi/spi-account-reference-creditor.json", SpiAccountReference.class);
        payment.setCreditorAccount(accountReferenceCreditor);
        payment.setCreditorAgent("BCENECEQ");
        payment.setCreditorId("27ad-46db-8491-71e629d82baa");
        payment.setCreditorName("Telekom");
        payment.setCreditorAddress(jsonReader.getObjectFromFile("json/support/mapper/spi/spi-address.json", SpiAddress.class));
        SpiAccountReference accountReferenceDebtor = jsonReader.getObjectFromFile("json/support/mapper/spi/spi-account-reference-debtor.json", SpiAccountReference.class);
        payment.setDebtorAccount(accountReferenceDebtor);
        payment.setEndToEndIdentification("RI-123456789");
        payment.setInstructedAmount(new SpiAmount(Currency.getInstance("EUR"), new BigDecimal("1000.00")));
        payment.setRemittanceInformationUnstructured("Ref. Number TELEKOM-1222");
        payment.setRemittanceInformationStructured("Ref Number Merchant");
        payment.setRequestedExecutionDate(OFFSET_DATE_TIME.toLocalDate());
        payment.setRequestedExecutionTime(OFFSET_DATE_TIME);
        payment.setPsuDataList(Collections.singletonList(SpiPsuData.builder()
                                                             .psuId("psuId")
                                                             .psuIdType("")
                                                             .psuCorporateId("")
                                                             .psuCorporateIdType("")
                                                             .psuIpAddress("")
                                                             .psuIpPort("")
                                                             .psuUserAgent("")
                                                             .psuGeoLocation("")
                                                             .psuAccept("")
                                                             .psuAcceptCharset("")
                                                             .psuAcceptEncoding("")
                                                             .psuAcceptLanguage("")
                                                             .psuHttpMethod("")
                                                             .psuDeviceId(UUID.randomUUID())
                                                             .build()));
        payment.setPurposeCode(PurposeCode.CDQC);
        payment.setStatusChangeTimestamp(OFFSET_DATE_TIME);
        payment.setUltimateCreditor("ultimateCreditor");
        payment.setUltimateDebtor("ultimateDebtor");
        return payment;
    }
}
