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

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

@Service
class AccountMapper {
    public List<AccountDetails> mapFromSpiAccountDetails(List<SpiAccountDetails> spiAccountDetailsList) {
        List<AccountDetails> accountDetails =
        Optional.ofNullable(spiAccountDetailsList)
        .map(acl ->
             acl
             .stream()
             .map(this::mapSpiAccountDetailsToXs2aAccountDetails)
             .collect(Collectors.toList())
        )
        .orElse(Collections.emptyList());
        String urlToAccount = linkTo(AccountController.class).toUriComponentsBuilder().build().getPath();
        accountDetails.forEach(account -> account.setBalanceAndTransactionLinksByDefault(urlToAccount));

        return accountDetails;
    }

    public AccountDetails mapSpiAccountDetailsToXs2aAccountDetails(SpiAccountDetails accountDetails) {
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
               mapAccountType(ad.getCashSpiAccountType()),
               ad.getBic(),
               mapSpiBalances(ad.getBalances()),
               new Links())
               ).orElse(null);
    }

    private CashAccountType mapAccountType(SpiAccountType spiAccountType) {
        return Optional.ofNullable(spiAccountType)
               .map(type -> CashAccountType.valueOf(type.name()))
               .orElse(null);
    }

    public Balances mapSpiBalances(SpiBalances spiBalances) {
        return Optional.ofNullable(spiBalances)
               .map(b -> {
                   Balances balances = new Balances();
                   balances.setAuthorised(mapSingleBalances(b.getAuthorised()));
                   balances.setClosing_booked(mapSingleBalances(b.getClosing_booked()));
                   balances.setClosingBooked(mapSingleBalances(b.getClosingBooked()));
                   balances.setExpected(mapSingleBalances(b.getExpected()));
                   balances.setInterimAvailable(mapSingleBalances(b.getInterimAvailable()));
                   balances.setOpeningBooked(mapSingleBalances(b.getOpeningBooked()));
                   return balances;
               })
               .orElse(null);
    }

    private SingleBalance mapSingleBalances(SpiAccountBalance spiAccountBalance) {
        return Optional.ofNullable(spiAccountBalance)
               .map(b -> {
                   SingleBalance singleBalance = new SingleBalance();
                   singleBalance.setAmount(mapSpiAmount(b.getSpiAmount()));
                   singleBalance.setDate(b.getDate());
                   singleBalance.setLastActionDateTime(b.getLastActionDateTime());
                   return singleBalance;
               })
               .orElse(null);
    }

    private Amount mapSpiAmount(SpiAmount spiAmount) {
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
               .map(a -> {
                   return new SpiAmount(a.getCurrency(), a.getContent());
               })
               .orElse(null);
    }

    public AccountReport mapAccountReport(List<SpiTransaction> spiTransactions) {
        if (spiTransactions == null) {
            return null;
        }

        Transactions[] booked = spiTransactions
                                .stream()
                                .filter(transaction -> transaction.getBookingDate() != null)
                                .map(this::mapSpiTransaction)
                                .toArray(Transactions[]::new);

        Transactions[] pending = spiTransactions
                                 .stream()
                                 .filter(transaction -> transaction.getBookingDate() == null)
                                 .map(this::mapSpiTransaction)
                                 .toArray(Transactions[]::new);

        return new AccountReport(booked, pending, new Links());
    }

    private Transactions mapSpiTransaction(SpiTransaction spiTransaction) {
        return Optional.ofNullable(spiTransaction)
               .map(t -> {
                   Transactions transactions = new Transactions();
                   transactions.setTransactionId(t.getTransactionId());
                   transactions.setEndToEndId(t.getEndToEndId());
                   transactions.setMandateId(t.getMandateId());
                   transactions.setCreditorId(t.getCreditorId());
                   transactions.setBookingDate(t.getBookingDate());
                   transactions.setValueDate(t.getValueDate());
                   transactions.setAmount(mapSpiAmount(t.getSpiAmount()));
                   transactions.setCreditorName(t.getCreditorName());
                   transactions.setCreditorAccount(mapSpiAccountReference(t.getCreditorAccount()));
                   transactions.setDebtorName(t.getDebtorName());
                   transactions.setDebtorAccount(mapSpiAccountReference(t.getDebtorAccount()));
                   transactions.setUltimateDebtor(t.getUltimateDebtor());
                   transactions.setRemittanceInformationUnstructured(t.getRemittanceInformationUnstructured());
                   transactions.setRemittanceInformationStructured(t.getRemittanceInformationStructured());
                   transactions.setPurposeCode(new PurposeCode(t.getPurposeCode()));
                   transactions.setBankTransactionCodeCode(new BankTransactionCode(t.getBankTransactionCodeCode()));
                   return transactions;
               })
               .orElse(null);
    }

    private AccountReference mapSpiAccountReference(SpiAccountReference spiAccountReference) {
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
}
