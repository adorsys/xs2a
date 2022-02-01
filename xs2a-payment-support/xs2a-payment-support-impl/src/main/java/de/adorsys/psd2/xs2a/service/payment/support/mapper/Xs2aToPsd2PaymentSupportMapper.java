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

package de.adorsys.psd2.xs2a.service.payment.support.mapper;

import de.adorsys.psd2.model.*;
import de.adorsys.psd2.xs2a.core.domain.address.Xs2aCountryCode;
import de.adorsys.psd2.xs2a.core.pis.PisDayOfExecution;
import de.adorsys.psd2.xs2a.domain.pis.BulkPayment;
import de.adorsys.psd2.xs2a.domain.pis.PeriodicPayment;
import de.adorsys.psd2.xs2a.domain.pis.SinglePayment;
import org.apache.commons.collections.CollectionUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Currency;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface Xs2aToPsd2PaymentSupportMapper {
    @Mapping(target = "creditorAgentName", ignore = true)
    @Mapping(target = "creditorAccount", expression = "java(mapToAccountReference(singlePayment.getCreditorAccount()))")
    @Mapping(target = "debtorAccount", expression = "java(mapToAccountReference(singlePayment.getDebtorAccount()))")
    PaymentInitiationJson mapToPaymentInitiationJson(SinglePayment singlePayment);

    @Mapping(target = "dayOfExecution", expression = "java(mapDayOfExecution(xs2aPeriodicPayment.getDayOfExecution()))")
    @Mapping(target = "remittanceInformationStructuredArray", expression = "java(mapToRemittanceInformationStructuredArray(xs2aPeriodicPayment.getRemittanceInformationStructuredArray()))")
    PeriodicPaymentInitiationJson mapToPeriodicPaymentInitiationJson(PeriodicPayment xs2aPeriodicPayment);

    BulkPaymentInitiationJson mapToBulkPaymentInitiationJson(BulkPayment xs2aBulkPayment);

    @Mapping(target = "currency", expression = "java(mapToCurrency(value.getCurrency()))")
    @Mapping(target = "other", expression = "java(mapToOtherType(value.getOther()))")
    AccountReference mapToAccountReference(de.adorsys.psd2.xs2a.core.profile.AccountReference value);

    default String mapToCountry(Xs2aCountryCode xs2aCountryCode) {
        return xs2aCountryCode.getCode();
    }

    default DayOfExecution mapDayOfExecution(PisDayOfExecution dayOfExecution) {
        return dayOfExecution == null
                   ? null
                   : DayOfExecution.fromValue(dayOfExecution.toString());
    }

    default RemittanceInformationStructuredArray mapToRemittanceInformationStructuredArray(List<String> remittanceInformationStructuredArray) {
        if (CollectionUtils.isEmpty(remittanceInformationStructuredArray)) {
            return null;
        }

        List<RemittanceInformationStructured> remittanceInfoStructuredList = remittanceInformationStructuredArray.stream()
                                                                                 .map(s -> new RemittanceInformationStructured().reference(s))
                                                                                 .collect(Collectors.toList());
        RemittanceInformationStructuredArray remittanceInfoStructuredArray = new RemittanceInformationStructuredArray();
        remittanceInfoStructuredArray.addAll(remittanceInfoStructuredList);
        return remittanceInfoStructuredArray;
    }

    default OtherType mapToOtherType(String other){
        return other == null
                   ? null
                   : new OtherType().identification(other);
    }

    default String mapToCurrency(Currency value){
        return value == null
                   ? null
                   : value.getCurrencyCode();
    }
}
