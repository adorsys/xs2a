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

import de.adorsys.psd2.model.BulkPaymentInitiationJson;
import de.adorsys.psd2.model.DayOfExecution;
import de.adorsys.psd2.model.PaymentInitiationJson;
import de.adorsys.psd2.model.PeriodicPaymentInitiationJson;
import de.adorsys.psd2.xs2a.core.domain.address.Xs2aCountryCode;
import de.adorsys.psd2.xs2a.core.pis.PisDayOfExecution;
import de.adorsys.psd2.xs2a.domain.pis.BulkPayment;
import de.adorsys.psd2.xs2a.domain.pis.PeriodicPayment;
import de.adorsys.psd2.xs2a.domain.pis.SinglePayment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface Xs2aToPsd2PaymentSupportMapper {
    @Mapping(target = "creditorAgentName", ignore = true)
    PaymentInitiationJson mapToPaymentInitiationJson(SinglePayment singlePayment);

    @Mapping(target = "dayOfExecution", expression = "java(mapDayOfExecution(xs2aPeriodicPayment.getDayOfExecution()))")
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
}
