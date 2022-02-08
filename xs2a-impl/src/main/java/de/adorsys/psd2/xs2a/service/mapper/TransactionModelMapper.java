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

package de.adorsys.psd2.xs2a.service.mapper;

import de.adorsys.psd2.model.*;
import de.adorsys.psd2.xs2a.core.pis.Remittance;
import de.adorsys.psd2.xs2a.domain.TransactionInfo;
import de.adorsys.psd2.xs2a.domain.Transactions;
import de.adorsys.psd2.xs2a.domain.account.Xs2aAccountReport;
import de.adorsys.psd2.xs2a.domain.account.Xs2aTransactionsReport;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.web.mapper.BalanceMapper;
import de.adorsys.psd2.xs2a.web.mapper.DayOfExecutionMapper;
import de.adorsys.psd2.xs2a.web.mapper.HrefLinkMapper;
import de.adorsys.psd2.xs2a.web.mapper.PurposeCodeMapper;
import de.adorsys.psd2.xs2a.web.mapper.RemittanceMapper;
import de.adorsys.psd2.xs2a.web.mapper.Xs2aAddressMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.jetbrains.annotations.Nullable;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Currency;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {
    AmountModelMapper.class, PurposeCodeMapper.class,
    Xs2aAddressMapper.class, AspspProfileServiceWrapper.class,
    ReportExchangeMapper.class, BalanceMapper.class,
    DayOfExecutionMapper.class, RemittanceMapper.class})
public abstract class TransactionModelMapper {
    @Autowired
    protected ReportExchangeMapper reportExchangeMapper;
    @Autowired
    protected HrefLinkMapper hrefLinkMapper;
    @Autowired
    protected BalanceMapper balanceMapper;
    @Autowired
    protected DayOfExecutionMapper dayOfExecutionMapper;
    @Autowired
    protected RemittanceMapper remittanceMapper;

    @Mapping(target = "_links", ignore = true)
    @Mapping(target = "links", expression = "java(hrefLinkMapper.mapToLinksMap(accountReport.getLinks()))")
    public abstract AccountReport mapToAccountReport(Xs2aAccountReport accountReport);

    @Mapping(target = "_links", ignore = true)
    @Mapping(target = "links", expression = "java(hrefLinkMapper.mapToLinksMap(transactionsReport.getLinks()))")
    @Mapping(target = "transactions", source = "accountReport")
    @Mapping(target = "account", expression = "java(mapToAccountReference(transactionsReport.getAccountReference()))")
    public abstract TransactionsResponse200Json mapToTransactionsResponse200Json(Xs2aTransactionsReport transactionsReport);

    public byte[] mapToTransactionsResponseRaw(Xs2aTransactionsReport transactionsReport) {
        return transactionsReport.getAccountReport().getTransactionsRaw();
    }

    @Mapping(target = "currencyExchange", expression = "java(reportExchangeMapper.mapToReportExchanges(transactions.getExchangeRate()))")
    @Mapping(target = "bankTransactionCode", source = "bankTransactionCodeCode.code")
    @Mapping(target = "transactionAmount", source = "amount")
    @Mapping(target = "additionalInformationStructured.standingOrderDetails.dayOfExecution", expression = "java(dayOfExecutionMapper.mapDayOfExecution(xs2aStandingOrderDetails.getDayOfExecution()))")
    @Mapping(target = "links", ignore = true)
    @Mapping(target = "_links", ignore = true)
    @Mapping(target = "creditorName", source = "transactionInfo.creditorName")
    @Mapping(target = "creditorAccount", expression = "java(mapToAccountReference(transactions.getTransactionInfo(), true))")
    @Mapping(target = "creditorAgent", source = "transactionInfo.creditorAgent")
    @Mapping(target = "ultimateCreditor", source = "transactionInfo.ultimateCreditor")
    @Mapping(target = "debtorName", source = "transactionInfo.debtorName")
    @Mapping(target = "debtorAccount", expression = "java(mapToAccountReference(transactions.getTransactionInfo(), false))")
    @Mapping(target = "debtorAgent", source = "transactionInfo.debtorAgent")
    @Mapping(target = "ultimateDebtor", source = "transactionInfo.ultimateDebtor")
    @Mapping(target = "remittanceInformationUnstructured", source = "transactionInfo.remittanceInformationUnstructured")
    @Mapping(target = "remittanceInformationUnstructuredArray", source = "transactionInfo")
    @Mapping(target = "remittanceInformationStructured", source = "transactionInfo")
    @Mapping(target = "remittanceInformationStructuredArray", expression = "java(mapToRemittanceInformationStructuredArray(transactions.getTransactionInfo()))")
    @Mapping(target = "purposeCode", source = "transactionInfo.purposeCode")
    public abstract de.adorsys.psd2.model.Transactions mapToTransactions(Transactions transactions);

    @Mapping(target = "creditorName", source = "transactionInfo.creditorName")
    @Mapping(target = "creditorAccount", expression = "java(mapToAccountReference(entryDetails.getTransactionInfo(), true))")
    @Mapping(target = "creditorAgent", source = "transactionInfo.creditorAgent")
    @Mapping(target = "ultimateCreditor", source = "transactionInfo.ultimateCreditor")
    @Mapping(target = "debtorName", source = "transactionInfo.debtorName")
    @Mapping(target = "debtorAccount", expression = "java(mapToAccountReference(entryDetails.getTransactionInfo(), false))")
    @Mapping(target = "debtorAgent", source = "transactionInfo.debtorAgent")
    @Mapping(target = "ultimateDebtor", source = "transactionInfo.ultimateDebtor")
    @Mapping(target = "remittanceInformationUnstructured", source = "transactionInfo.remittanceInformationUnstructured")
    @Mapping(target = "remittanceInformationUnstructuredArray", source = "transactionInfo")
    @Mapping(target = "remittanceInformationStructured", source = "transactionInfo")
    @Mapping(target = "remittanceInformationStructuredArray", source = "transactionInfo")
    @Mapping(target = "purposeCode", source = "transactionInfo.purposeCode")
    public abstract de.adorsys.psd2.model.EntryDetailsElement mapToEntryDetailsElement(de.adorsys.psd2.xs2a.domain.EntryDetails entryDetails);

    public InlineResponse2001 mapToTransactionDetails(Transactions transactions) {
        InlineResponse2001 inlineResponse2001 = new InlineResponse2001();
        inlineResponse2001.setTransactionsDetails(mapToTransactions(transactions));
        return inlineResponse2001;
    }

    protected RemittanceInformationStructuredMax140 mapToRemittanceInformationStructuredMax140(TransactionInfo transactionInfo) {
        if (transactionInfo == null || transactionInfo.getRemittanceInformationStructured() == null) {
            return null;
        }
        Remittance remittanceInformationStructured = transactionInfo.getRemittanceInformationStructured();
        RemittanceInformationStructuredMax140 structuredMax140 = new RemittanceInformationStructuredMax140();
        structuredMax140.setReference(remittanceInformationStructured.getReference());
        structuredMax140.setReferenceType(remittanceInformationStructured.getReferenceType());
        structuredMax140.setReferenceIssuer(remittanceInformationStructured.getReferenceIssuer());
        return structuredMax140;
    }

    protected RemittanceInformationStructured mapToRemittanceInformationStructured(TransactionInfo transactionInfo) {
        if (transactionInfo == null || transactionInfo.getRemittanceInformationStructured() == null) {
            return null;
        }
        Remittance remittanceInformationStructured = transactionInfo.getRemittanceInformationStructured();
        RemittanceInformationStructured structured = new RemittanceInformationStructured();
        structured.setReference(remittanceInformationStructured.getReference());
        structured.setReferenceType(remittanceInformationStructured.getReferenceType());
        structured.setReferenceIssuer(remittanceInformationStructured.getReferenceIssuer());
        return structured;
    }


    protected @Nullable TransactionList mapToTransactionList(@Nullable List<Transactions> transactions) {
        if (CollectionUtils.isEmpty(transactions)) {
            return null;
        }

        List<de.adorsys.psd2.model.Transactions> transactionDetails = transactions.stream()
                                                                          .map(this::mapToTransactions)
                                                                          .collect(Collectors.toList());

        TransactionList transactionList = new TransactionList();
        transactionList.addAll(transactionDetails);
        return transactionList;
    }

    protected RemittanceInformationUnstructuredArray mapToRemittanceInformationUnstructuredArray(TransactionInfo transactionInfo) {
        if (transactionInfo == null || CollectionUtils.isEmpty(transactionInfo.getRemittanceInformationUnstructuredArray())) {
            return null;
        }

        RemittanceInformationUnstructuredArray remittanceUnstructuredArray = new RemittanceInformationUnstructuredArray();
        remittanceUnstructuredArray.addAll(transactionInfo.getRemittanceInformationUnstructuredArray());
        return remittanceUnstructuredArray;
    }

    protected RemittanceInformationStructuredArray mapToRemittanceInformationStructuredArray(TransactionInfo transactionInfo) {
        if (transactionInfo == null || CollectionUtils.isEmpty(transactionInfo.getRemittanceInformationStructuredArray())) {
            return null;
        }

        List<RemittanceInformationStructured> remittanceInfoStructuredList = transactionInfo.getRemittanceInformationStructuredArray().stream()
                                                                                 .map(this::mapToRemittanceInformationStructured)
                                                                                 .collect(Collectors.toList());
        RemittanceInformationStructuredArray remittanceInfoStructuredArray = new RemittanceInformationStructuredArray();
        remittanceInfoStructuredArray.addAll(remittanceInfoStructuredList);
        return remittanceInfoStructuredArray;
    }

    protected RemittanceInformationStructured mapToRemittanceInformationStructured(Remittance remittance) {
        if (remittance == null) {
            return null;
        }
        RemittanceInformationStructured remittanceInformationStructured = new RemittanceInformationStructured();
        remittanceInformationStructured.setReference(remittance.getReference());
        remittanceInformationStructured.setReferenceType(remittance.getReferenceType());
        remittanceInformationStructured.setReferenceIssuer(remittance.getReferenceIssuer());
        return remittanceInformationStructured;
    }

    @Mapping(target = "currency", expression = "java(mapToCurrency(value.getCurrency()))")
    @Mapping(target = "other", expression = "java(mapToOtherType(value.getOther()))")
    public abstract AccountReference mapToAccountReference(de.adorsys.psd2.xs2a.core.profile.AccountReference value);

    protected AccountReference mapToAccountReference(de.adorsys.psd2.xs2a.domain.TransactionInfo transactionInfo, boolean isCreditorAccount) {
        if (transactionInfo == null ) {
            return null;
        }
        de.adorsys.psd2.xs2a.core.profile.AccountReference value = isCreditorAccount
                                                                       ? transactionInfo.getCreditorAccount()
                                                                       : transactionInfo.getDebtorAccount();
        return mapToAccountReference(value);
    }

    protected OtherType mapToOtherType(String other){
        return other == null
                   ? null
                   : new OtherType().identification(other);
    }

    protected String mapToCurrency(Currency value){
        return value == null
                   ? null
                   : value.getCurrencyCode();
    }
}
