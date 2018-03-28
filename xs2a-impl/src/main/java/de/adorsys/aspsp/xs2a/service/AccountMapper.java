package de.adorsys.aspsp.xs2a.service;

import de.adorsys.aspsp.xs2a.domain.*;
import de.adorsys.aspsp.xs2a.domain.code.BankTransactionCode;
import de.adorsys.aspsp.xs2a.domain.code.PurposeCode;
import de.adorsys.aspsp.xs2a.spi.domain.account.*;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiAmount;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
class AccountMapper {
    public AccountDetails mapSpiAccountDetailsToXs2aAccountDetails(SpiAccountDetails accountDetails) {
        return Optional.ofNullable(accountDetails)
            .map(accountDetail -> new AccountDetails(
                    accountDetail.getId(),
                    accountDetail.getIban(),
                    accountDetail.getBban(),
                    accountDetail.getPan(),
                    accountDetail.getMaskedPan(),
                    accountDetail.getMsisdn(),
                    accountDetail.getCurrency(),
                    accountDetail.getName(),
                    accountDetail.getAccountType(),
                    mapAccountType(accountDetail.getCashSpiAccountType()),
                    accountDetail.getBic(),
                    mapSpiBalances(accountDetail.getBalances()),
                    new Links()
                )
            )
            .orElse(null);
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

    public AccountReport mapAccountReport(List<SpiTransaction> spiTransactions) {
        if (spiTransactions == null) {
            return null;
        }

        Transaction[] booked = spiTransactions
            .stream()
            .filter(transaction -> transaction.getBookingDate() != null)
            .map(this::mapSpiTransaction)
            .toArray(Transaction[]::new);

        Transaction[] pending = spiTransactions
            .stream()
            .filter(transaction -> transaction.getBookingDate() == null)
            .map(this::mapSpiTransaction)
            .toArray(Transaction[]::new);

        return new AccountReport(booked, pending, new Links());
    }

    private Transaction mapSpiTransaction(SpiTransaction spiTransaction) {
        return Optional.ofNullable(spiTransaction)
            .map(t -> {
                Transaction transaction = new Transaction();
                transaction.setAmount(mapSpiAmount(t.getSpiAmount()));
                transaction.setBankTransactionCodeCode(new BankTransactionCode(t.getBankTransactionCodeCode()));
                transaction.setBookingDate(t.getBookingDate());
                transaction.setCreditorAccount(mapSpiAccountReference(t.getCreditorAccount()));
                transaction.setDebtorAccount(mapSpiAccountReference(t.getDebtorAccount()));
                transaction.setCreditorId(t.getCreditorId());
                transaction.setCreditorName(t.getCreditorName());
                transaction.setDebtorName(t.getDebtorName());
                transaction.setUltimateDebtor(t.getUltimateDebtor());
                transaction.setEndToEndId(t.getEndToEndId());
                transaction.setMandateId(t.getMandateId());
                transaction.setPurposeCode(new PurposeCode(t.getPurposeCode()));
                transaction.setTransactionId(t.getTransactionId());
                transaction.setRemittanceInformationStructured(t.getRemittanceInformationStructured());
                transaction.setRemittanceInformationUnstructured(t.getRemittanceInformationUnstructured());
                return transaction;
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
                accountReference.setMaskedPan(ar.getMsisdn());
                accountReference.setCurrency(ar.getCurrency());
                return accountReference;
            })
            .orElse(null);

    }
}
