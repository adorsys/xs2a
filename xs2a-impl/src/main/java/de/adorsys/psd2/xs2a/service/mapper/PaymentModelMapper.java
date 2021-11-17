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

package de.adorsys.psd2.xs2a.service.mapper;

import de.adorsys.psd2.model.*;
import de.adorsys.psd2.xs2a.core.pis.PisDayOfExecution;
import de.adorsys.psd2.xs2a.core.pis.Xs2aAmount;
import de.adorsys.psd2.xs2a.domain.pis.BulkPayment;
import de.adorsys.psd2.xs2a.domain.pis.PeriodicPayment;
import de.adorsys.psd2.xs2a.domain.pis.SinglePayment;
import de.adorsys.psd2.xs2a.web.mapper.PurposeCodeMapper;
import de.adorsys.psd2.xs2a.web.mapper.RemittanceMapper;
import de.adorsys.psd2.xs2a.web.mapper.Xs2aAddressMapper;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring",
    uses = {Xs2aAddressMapper.class, RemittanceMapper.class, PurposeCodeMapper.class},
    imports = RemittanceInformationStructured.class)
public interface PaymentModelMapper {

    @Mapping(target = "dayOfExecution", expression = "java(mapDayOfExecution(paymentRequest.getDayOfExecution()))")
    @Mapping(target = "remittanceInformationStructuredArray", expression = "java(mapToRemittanceInformationStructuredString(paymentRequest.getRemittanceInformationStructuredArray()))")
    PeriodicPayment mapToXs2aPayment(PeriodicPaymentInitiationJson paymentRequest);

    SinglePayment mapToXs2aPayment(PaymentInitiationJson paymentRequest);

    BulkPayment mapToXs2aPayment(BulkPaymentInitiationJson paymentRequest);

    Xs2aAmount mapToXs2aAmount(Amount amount);

    @Mapping(target = "other", source = "other.identification")
    de.adorsys.psd2.xs2a.core.profile.AccountReference mapToAccountReference(AccountReference accountReference);

    @AfterMapping
    default void mapToXs2aPaymentAfterMapping(BulkPaymentInitiationJson paymentRequest,
                                              @MappingTarget BulkPayment bulkPayment) {
        LocalDate requestedExecutionDate = paymentRequest.getRequestedExecutionDate();
        OffsetDateTime requestedExecutionTime = paymentRequest.getRequestedExecutionTime();
        de.adorsys.psd2.xs2a.core.profile.AccountReference debtorAccount = mapToAccountReference(paymentRequest.getDebtorAccount());

        bulkPayment.getPayments().forEach(bp -> {
            bp.setRequestedExecutionDate(requestedExecutionDate);
            bp.setRequestedExecutionTime(requestedExecutionTime);
            bp.setDebtorAccount(debtorAccount);
        });
    }

    default PisDayOfExecution mapDayOfExecution(DayOfExecution dayOfExecution) {
        if (dayOfExecution != null) {
            Optional<PisDayOfExecution> pisDayOfExecutionOptional = PisDayOfExecution.getByValue(dayOfExecution.toString());
            return pisDayOfExecutionOptional.orElse(null);
        }
        return null;
    }

    default List<String> mapToRemittanceInformationStructuredString(RemittanceInformationStructuredArray value) {
        if (value == null) {
            return Collections.emptyList();
        }
        return value.stream().map(RemittanceInformationStructured::getReference).collect(Collectors.toList());
    }
}
