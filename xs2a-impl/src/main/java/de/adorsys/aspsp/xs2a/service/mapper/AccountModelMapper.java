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

import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.aspsp.xs2a.domain.*;
import de.adorsys.aspsp.xs2a.domain.Amount;
import de.adorsys.aspsp.xs2a.domain.Balance;
import de.adorsys.aspsp.xs2a.domain.account.AccountDetails;
import de.adorsys.aspsp.xs2a.domain.account.AccountReference;
import de.adorsys.aspsp.xs2a.domain.account.AccountReport;
import de.adorsys.psd2.model.*;
import de.adorsys.psd2.model.BalanceType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;

@Slf4j
public final class AccountModelMapper {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static AccountList mapToAccountList(Map<String, List<AccountDetails>> accountDetailsList) {
        List<de.adorsys.psd2.model.AccountDetails> details = accountDetailsList.values().stream()
            .flatMap(al -> al.stream().map(AccountModelMapper::mapToAccountDetails))
            .collect(Collectors.toList());
        return new AccountList().accounts(details);
    }

    public static de.adorsys.psd2.model.AccountDetails mapToAccountDetails(AccountDetails accountDetails) {
        de.adorsys.psd2.model.AccountDetails detailsTarget = new de.adorsys.psd2.model.AccountDetails();
        BeanUtils.copyProperties(accountDetails, detailsTarget);


        detailsTarget.resourceId(accountDetails.getId())
            .currency(Optional.ofNullable(accountDetails.getCurrency()).map(c -> c.getCurrencyCode()).orElse(null))
//                    .product(accountDetails.get???) TODO fill value
            .cashAccountType(Optional.ofNullable(accountDetails.getCashAccountType()).map(c -> c.name()).orElse(null));
//                    .status(accountDetails.get???) TODO fill value
//                    .usage(accountDetails.get???) TODO fill value
//                    .details(accountDetails.get???) TODO fill value

        detailsTarget.setBalances(new BalanceList());

        accountDetails.getBalances().forEach(balance ->
            detailsTarget.getBalances().add(mapToBalance(balance))
        );

        detailsTarget.setLinks(OBJECT_MAPPER.convertValue(accountDetails.getLinks(), Map.class));

        return detailsTarget;
    }

    public static ReadBalanceResponse200 mapToBalance(List<Balance> balances) {
        ReadBalanceResponse200 response = new ReadBalanceResponse200();
        BalanceList balancesResponse = new BalanceList();
        response.setBalances(balancesResponse);

        balances.forEach(balance ->
            response.getBalances().add(mapToBalance(balance))
        );

        return response;
    }

    public static de.adorsys.psd2.model.Balance mapToBalance(Balance balance) {
        de.adorsys.psd2.model.Balance target = new de.adorsys.psd2.model.Balance();
        BeanUtils.copyProperties(balance, target);

        target.setBalanceAmount(mapToAmount(balance.getBalanceAmount()));
        if (balance.getBalanceType() != null) {
            target.setBalanceType(mapBalanceType(balance.getBalanceType()));
        }
        LocalDateTime ldt = balance.getLastChangeDateTime();
        if (ldt != null) {
            List<ZoneOffset> validOffsets = ZoneId.systemDefault().getRules().getValidOffsets(ldt);
            target.setLastChangeDateTime(ldt.atOffset(validOffsets.get(0)));
        }
        return target;
    }

    private static de.adorsys.psd2.model.Amount mapToAmount(Amount amount) {
        de.adorsys.psd2.model.Amount amountTarget = new de.adorsys.psd2.model.Amount();
        amountTarget.setAmount(amount.getContent());
        amountTarget.setCurrency(amount.getCurrency().getCurrencyCode());
        return amountTarget;
    }

    public static de.adorsys.psd2.model.AccountReport mapToAccountReport(AccountReport accountReport) {
        de.adorsys.psd2.model.AccountReport target = new de.adorsys.psd2.model.AccountReport();

        TransactionList booked = new TransactionList();
        List<TransactionDetails> list = Optional.ofNullable(accountReport.getBooked())
            .map(ts -> Arrays.stream(ts).map(AccountModelMapper::mapToTransaction).collect(Collectors.toList()))
            .orElse(Collections.EMPTY_LIST);
        booked.addAll(list);
        target.setBooked(booked);

        TransactionList pending = new TransactionList();
        List<TransactionDetails> list2 = Optional.ofNullable(accountReport.getPending())
            .map(ts -> Arrays.stream(ts).map(AccountModelMapper::mapToTransaction).collect(Collectors.toList()))
            .orElse(Collections.EMPTY_LIST);
        pending.addAll(list2);
        target.setPending(pending);

        target.setLinks(OBJECT_MAPPER.convertValue(accountReport.getLinks(), Map.class));

        return target;
    }

    public static TransactionDetails mapToTransaction(Transactions transactions) {
        TransactionDetails transactionDetails = new TransactionDetails();
        BeanUtils.copyProperties(transactions, transactionDetails);

        //transform Account info
        transactionDetails.setCreditorAccount(createAccountObject(transactions.getCreditorAccount()));
        transactionDetails.setDebtorAccount(createAccountObject(transactions.getDebtorAccount()));

//        transactionDetails.setEntryReference(t.get???); TODO fill value
//        transactionDetails.setCheckId(t.get???); TODO fill value

        if (transactions.getAmount() != null) {
            Amount amount = transactions.getAmount();
            transactionDetails.setTransactionAmount(
                new de.adorsys.psd2.model.Amount().amount(amount.getContent()).currency(amount.getCurrency().getCurrencyCode())
            );
        }

//        transactionDetails.setExchangeRate(t.get???); TODO fill value
        try {
            transactionDetails.setPurposeCode(PurposeCode.valueOf(transactions.getPurposeCode().getCode()));
        } catch (IllegalArgumentException e) {
            log.error("Exception in mapping transaction purpose code", e);
        }
        if (transactions.getBankTransactionCodeCode() != null) {
            transactionDetails.setBankTransactionCode(transactions.getBankTransactionCodeCode().getCode());
        }
//        transactionDetails.setProprietaryBankTransactionCode(t.get???); TODO fill value
//        transactionDetails.setLinks(t.get???); TODO fill value

        return transactionDetails;
    }

    private static Object createAccountObject(AccountReference accountReference) {
        if (accountReference == null) {
            return null;
        }

        // Iban, Bban, MaskedPan, Msisdn, Pan
        if (accountReference.getIban() != null) {
            return new AccountReferenceIban()
                .iban(accountReference.getIban())
                .currency(Optional.ofNullable(accountReference.getCurrency()).map(Currency::getCurrencyCode).orElse(null));
        } else if (accountReference.getBban() != null) {
            return new AccountReferenceBban()
                .bban(accountReference.getBban())
                .currency(Optional.ofNullable(accountReference.getCurrency()).map(Currency::getCurrencyCode).orElse(null));
        } else if (accountReference.getMaskedPan() != null) {
            return new AccountReferenceMaskedPan()
                .maskedPan(accountReference.getMaskedPan())
                .currency(Optional.ofNullable(accountReference.getCurrency()).map(Currency::getCurrencyCode).orElse(null));
        } else if (accountReference.getMsisdn() != null) {
            return new AccountReferenceMsisdn()
                .msisdn(accountReference.getMsisdn())
                .currency(Optional.ofNullable(accountReference.getCurrency()).map(Currency::getCurrencyCode).orElse(null));
        } else if (accountReference.getPan() != null) {
            return new AccountReferencePan()
                .pan(accountReference.getPan())
                .currency(Optional.ofNullable(accountReference.getCurrency()).map(Currency::getCurrencyCode).orElse(null));
        }

        return null;
    }

    private static BalanceType mapBalanceType(de.adorsys.aspsp.xs2a.domain.BalanceType balanceType) {
        switch (balanceType) {
            case CLOSING_BOOKED:
                return BalanceType.CLOSINGBOOKED;
            case EXPECTED:
                return BalanceType.EXPECTED;
            case AUTHORISED:
                return BalanceType.AUTHORISED;
            case OPENING_BOOKED:
                return BalanceType.OPENINGBOOKED;
            case INTERIM_AVAILABLE:
                return BalanceType.INTERIMAVAILABLE;
            case FORWARD_AVAILABLE:
                return BalanceType.FORWARDAVAILABLE;
            default:
                return null;
        }
    }
}
