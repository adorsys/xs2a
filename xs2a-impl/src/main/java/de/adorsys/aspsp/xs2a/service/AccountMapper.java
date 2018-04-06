package de.adorsys.aspsp.xs2a.service;

import de.adorsys.aspsp.xs2a.domain.*;
import de.adorsys.aspsp.xs2a.domain.code.BankTransactionCode;
import de.adorsys.aspsp.xs2a.domain.code.PurposeCode;
import de.adorsys.aspsp.xs2a.spi.domain.account.*;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiAmount;
import de.adorsys.aspsp.xs2a.web.AccountController;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

@Service
class AccountMapper {
    public List<AccountDetails> mapFromSpiAccountDetailsList(List<SpiAccountDetails> spiAccountDetailsList) {
        String urlToAccount = linkTo(AccountController.class).toUriComponentsBuilder().build().getPath();

        return Optional.ofNullable(spiAccountDetailsList)
               .map(acl -> acl.stream()
                           .map(this::mapFromSpiAccountDetails)
                           .peek(account -> account.setBalanceAndTransactionLinksByDefault(urlToAccount))
                           .collect(Collectors.toList()))
               .orElse(Collections.emptyList());
    }

    public AccountDetails mapFromSpiAccountDetails(SpiAccountDetails accountDetails) {
        return Optional.ofNullable(accountDetails)
               .map(ad -> new AccountDetails(
               ad.getId(),
               ad.getIban(),
               ad.getBban(),
               ad.getPan(),
               ad.getMaskedPan(),
               ad.getMsisdn(),
               ad.getCurrency(),
               ad.getName(),
               ad.getAccountType(),
               mapFromSpiAccountType(ad.getCashSpiAccountType()),
               ad.getBic(),
               mapFromSpiBalancesList(ad.getBalances()),
               new Links()
               )
               )
               .orElse(null);
    }

    private CashAccountType mapFromSpiAccountType(SpiAccountType spiAccountType) {
        return Optional.ofNullable(spiAccountType)
               .map(type -> CashAccountType.valueOf(type.name()))
               .orElse(null);
    }

    public List<Balances> mapFromSpiBalancesList(List<SpiBalances> spiBalances) {
        if (spiBalances == null) {
            return null;
        }

        List<Balances> balances = spiBalances
                                  .stream()
                                  .map(this::mapFromSpiBalances)
                                  .collect(Collectors.toList());

        return balances;
    }

    private Balances mapFromSpiBalances(SpiBalances spiBalances) {
        return Optional.ofNullable(spiBalances)
               .map(b -> {
                   Balances balances = new Balances();
                   balances.setAuthorised(mapFromSpiSingleBalances(b.getAuthorised()));
                   balances.setClosingBooked(mapFromSpiSingleBalances(b.getClosingBooked()));
                   balances.setExpected(mapFromSpiSingleBalances(b.getExpected()));
                   balances.setInterimAvailable(mapFromSpiSingleBalances(b.getInterimAvailable()));
                   balances.setOpeningBooked(mapFromSpiSingleBalances(b.getOpeningBooked()));
                   return balances;
               })
               .orElse(null);
    }

    private SingleBalance mapFromSpiSingleBalances(SpiAccountBalance spiAccountBalance) {
        return Optional.ofNullable(spiAccountBalance)
               .map(b -> {
                   SingleBalance singleBalance = new SingleBalance();
                   singleBalance.setAmount(mapFromSpiAmount(b.getSpiAmount()));
                   singleBalance.setDate(b.getDate());
                   singleBalance.setLastActionDateTime(b.getLastActionDateTime());
                   return singleBalance;
               })
               .orElse(null);
    }

    private Amount mapFromSpiAmount(SpiAmount spiAmount) {
        return Optional.ofNullable(spiAmount)
               .map(a -> {
                   Amount amount = new Amount();
                   amount.setContent(a.getContent());
                   amount.setCurrency(a.getCurrency());
                   return amount;
               })
               .orElse(null);
    }

    public SpiAmount mapToSpiAmount(Amount amount) {
        return Optional.ofNullable(amount)
               .map(a -> new SpiAmount(a.getCurrency(), a.getContent()))
               .orElse(null);
    }

    public AccountReport mapFromSpiAccountReport(List<SpiTransaction> spiTransactions) {
        if (spiTransactions == null) {
            return null;
        }

        Transactions[] booked = spiTransactions
                                .stream()
                                .filter(transaction -> transaction.getBookingDate() != null)
                                .map(this::mapFromSpiTransaction)
                                .toArray(Transactions[]::new);

        Transactions[] pending = spiTransactions
                                 .stream()
                                 .filter(transaction -> transaction.getBookingDate() == null)
                                 .map(this::mapFromSpiTransaction)
                                 .toArray(Transactions[]::new);

        return new AccountReport(booked, pending, new Links());
    }

    private Transactions mapFromSpiTransaction(SpiTransaction spiTransaction) {
        return Optional.ofNullable(spiTransaction)
               .map(t -> {
                   Transactions transactions = new Transactions();
                   transactions.setAmount(mapFromSpiAmount(t.getSpiAmount()));
                   transactions.setBankTransactionCodeCode(new BankTransactionCode(t.getBankTransactionCodeCode()));
                   transactions.setBookingDate(t.getBookingDate());
                   transactions.setValueDate(t.getValueDate());
                   transactions.setCreditorAccount(mapFromSpiAccountReference(t.getCreditorAccount()));
                   transactions.setDebtorAccount(mapFromSpiAccountReference(t.getDebtorAccount()));
                   transactions.setCreditorId(t.getCreditorId());
                   transactions.setCreditorName(t.getCreditorName());
                   transactions.setUltimateCreditor(t.getUltimateCreditor());
                   transactions.setDebtorName(t.getDebtorName());
                   transactions.setUltimateDebtor(t.getUltimateDebtor());
                   transactions.setEndToEndId(t.getEndToEndId());
                   transactions.setMandateId(t.getMandateId());
                   transactions.setPurposeCode(new PurposeCode(t.getPurposeCode()));
                   transactions.setTransactionId(t.getTransactionId());
                   transactions.setRemittanceInformationStructured(t.getRemittanceInformationStructured());
                   transactions.setRemittanceInformationUnstructured(t.getRemittanceInformationUnstructured());
                   return transactions;
               })
               .orElse(null);
    }

    private AccountReference mapFromSpiAccountReference(SpiAccountReference spiAccountReference) {
        return Optional.ofNullable(spiAccountReference)
               .map(ar -> {
                   AccountReference accountReference = new AccountReference();
                   accountReference.setAccountId(ar.getAccountId());
                   accountReference.setIban(ar.getIban());
                   accountReference.setBban(ar.getBban());
                   accountReference.setPan(ar.getPan());
                   accountReference.setMaskedPan(ar.getMaskedPan());
                   accountReference.setMsisdn(ar.getMsisdn());
                   accountReference.setCurrency(ar.getCurrency());
                   return accountReference;
               })
               .orElse(null);

    }

    public SpiAccountReference toSpi(AccountReference account){
        return ofNullable(account)
            .map(ac -> new SpiAccountReference(ac.getAccountId(),
                ac.getIban(),
                ac.getBban(),
                ac.getPan(),
                ac.getMaskedPan(),
                ac.getMsisdn(),
                ac.getCurrency()))
            .orElse(null);
    }

    public SpiAmount toSpi(Amount amount){
        return ofNullable(amount)
            .map(am -> new SpiAmount(am.getCurrency(), am.getContent()))
        .orElse(null);
    }
}
