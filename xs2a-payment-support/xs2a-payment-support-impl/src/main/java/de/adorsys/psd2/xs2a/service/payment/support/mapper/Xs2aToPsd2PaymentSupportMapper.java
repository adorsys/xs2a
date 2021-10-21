/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.service.payment.support.mapper;

import de.adorsys.psd2.model.*;
import de.adorsys.psd2.xs2a.core.domain.address.Xs2aCountryCode;
import de.adorsys.psd2.xs2a.core.pis.PisDayOfExecution;
import de.adorsys.psd2.xs2a.domain.pis.BulkPayment;
import de.adorsys.psd2.xs2a.domain.pis.PeriodicPayment;
import de.adorsys.psd2.xs2a.domain.pis.SinglePayment;
import org.apache.commons.collections4.CollectionUtils;
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

    default String mapToCountry(Xs2aCountryCode xs2aCountryCode) {
        return xs2aCountryCode.getCode();
    }

    default DayOfExecution mapDayOfExecution(PisDayOfExecution dayOfExecution) {
        return dayOfExecution != null
                   ? DayOfExecution.fromValue(dayOfExecution.toString())
                   : null;
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

    default AccountReference mapToAccountReference(de.adorsys.psd2.xs2a.core.profile.AccountReference value) {
        if (value == null ) {
            return null;
        }
        AccountReference accountReference = new AccountReference();
        accountReference.setIban(value.getIban());
        accountReference.setBban(value.getBban());
        accountReference.setPan(value.getPan());
        accountReference.setMaskedPan(value.getMaskedPan());
        accountReference.setMsisdn(value.getMsisdn());
        accountReference.setCurrency(mapToCurrency(value.getCurrency()));
        accountReference.setOther(mapToOtherType(value.getOther()));
        accountReference.cashAccountType(value.getCashAccountType());
        return accountReference;
    }

    default OtherType mapToOtherType(String other){
        return other == null ? null : new OtherType().identification(other);
    }

    default String mapToCurrency(Currency value){
        return value == null ? null : value.getCurrencyCode();
    }
}
