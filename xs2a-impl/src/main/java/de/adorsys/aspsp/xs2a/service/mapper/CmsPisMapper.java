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

package de.adorsys.aspsp.xs2a.service.mapper;

import de.adorsys.aspsp.xs2a.consent.api.pis.PisAddress;
import de.adorsys.aspsp.xs2a.consent.api.pis.PisPeriodicPayment;
import de.adorsys.aspsp.xs2a.consent.api.pis.PisRemittance;
import de.adorsys.aspsp.xs2a.consent.api.pis.PisSinglePayment;
import de.adorsys.aspsp.xs2a.domain.address.Address;
import de.adorsys.aspsp.xs2a.domain.code.BICFI;
import de.adorsys.aspsp.xs2a.domain.code.PurposeCode;
import de.adorsys.aspsp.xs2a.domain.pis.PeriodicPayment;
import de.adorsys.aspsp.xs2a.domain.pis.Remittance;
import de.adorsys.aspsp.xs2a.domain.pis.SinglePayments;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CmsPisMapper {
    private AccountMapper accountMapper;
    private PaymentMapper paymentMapper;

    public PisSinglePayment mapToPisSinglePayment(SinglePayments paymentInitiationRequest) {
        return Optional.ofNullable(paymentInitiationRequest)
                   .map(payReq -> {
                       PisSinglePayment pisSinglePayment = new PisSinglePayment();
                       pisSinglePayment.setEndToEndIdentification(payReq.getEndToEndIdentification());
                       pisSinglePayment.setDebtorAccount(accountMapper.mapToPisAccountReference(payReq.getDebtorAccount()));
                       pisSinglePayment.setUltimateDebtor(payReq.getUltimateDebtor());
                       pisSinglePayment.setInstructedAmount(accountMapper.mapToPisAmount(payReq.getInstructedAmount()));
                       pisSinglePayment.setCreditorAccount(accountMapper.mapToPisAccountReference(payReq.getCreditorAccount()));
                       pisSinglePayment.setCreditorAgent(Optional.ofNullable(payReq.getCreditorAgent())
                                                             .map(BICFI::getCode).orElse(""));
                       pisSinglePayment.setCreditorName(payReq.getCreditorName());
                       pisSinglePayment.setCreditorAddress(mapToPisAddress(payReq.getCreditorAddress()));
                       pisSinglePayment.setUltimateCreditor(payReq.getUltimateCreditor());
                       pisSinglePayment.setPurposeCode(Optional.ofNullable(payReq.getPurposeCode())
                                                           .map(PurposeCode::getCode).orElse(""));
                       pisSinglePayment.setRemittanceInformationUnstructured(payReq.getRemittanceInformationUnstructured());
                       pisSinglePayment.setRemittanceInformationStructured(mapToPisRemittance(payReq.getRemittanceInformationStructured()));
                       pisSinglePayment.setRequestedExecutionDate(payReq.getRequestedExecutionDate());
                       pisSinglePayment.setRequestedExecutionTime(payReq.getRequestedExecutionTime());

                       return pisSinglePayment;
                   })
                   .orElse(null);
    }

    public List<PisSinglePayment> mapToPisSinglePaymentList(List<SinglePayments> singlePayments) {
        return singlePayments.stream()
                   .map(this::mapToPisSinglePayment)
                   .collect(Collectors.toList());
    }

    public PisPeriodicPayment mapToPisPeriodicPayment(PeriodicPayment periodicPayment) {
        return Optional.ofNullable(periodicPayment)
                   .map(pp -> {
                       PisPeriodicPayment pisPeriodicPayment = new PisPeriodicPayment();
                       pisPeriodicPayment.setEndToEndIdentification(pp.getEndToEndIdentification());
                       pisPeriodicPayment.setDebtorAccount(accountMapper.mapToPisAccountReference(pp.getDebtorAccount()));
                       pisPeriodicPayment.setUltimateDebtor(pp.getUltimateDebtor());
                       pisPeriodicPayment.setInstructedAmount(accountMapper.mapToPisAmount(pp.getInstructedAmount()));
                       pisPeriodicPayment.setCreditorAccount(accountMapper.mapToPisAccountReference(pp.getCreditorAccount()));
                       pisPeriodicPayment.setCreditorAgent(paymentMapper.getCreditorAgentCode(pp));
                       pisPeriodicPayment.setCreditorName(pp.getCreditorName());
                       pisPeriodicPayment.setCreditorAddress(mapToPisAddress(pp.getCreditorAddress()));
                       pisPeriodicPayment.setUltimateCreditor(pp.getUltimateCreditor());
                       pisPeriodicPayment.setPurposeCode(paymentMapper.getPurposeCode(pp));
                       pisPeriodicPayment.setRemittanceInformationUnstructured(pp.getRemittanceInformationUnstructured());
                       pisPeriodicPayment.setRemittanceInformationStructured(mapToPisRemittance(pp.getRemittanceInformationStructured()));
                       pisPeriodicPayment.setRequestedExecutionDate(pp.getRequestedExecutionDate());
                       pisPeriodicPayment.setRequestedExecutionTime(pp.getRequestedExecutionTime());
                       pisPeriodicPayment.setStartDate(pp.getStartDate());
                       pisPeriodicPayment.setExecutionRule(pp.getExecutionRule());
                       pisPeriodicPayment.setEndDate(pp.getEndDate());
                       pisPeriodicPayment.setFrequency(paymentMapper.getFrequency(pp));
                       pisPeriodicPayment.setDayOfExecution(pp.getDayOfExecution());

                       return pisPeriodicPayment;
                   })
                   .orElse(null);
    }

    private PisAddress mapToPisAddress(Address address) {
        return Optional.ofNullable(address)
                   .map(a -> new PisAddress(a.getStreet(), a.getBuildingNumber(), a.getCity(), a.getPostalCode(), a.getCountry().getCode()))
                   .orElse(null);
    }

    private PisRemittance mapToPisRemittance(Remittance remittance) {
        return Optional.ofNullable(remittance)
                   .map(r -> new PisRemittance(r.getReference(), r.getReferenceType(), r.getReferenceIssuer()))
                   .orElse(null);
    }

}
