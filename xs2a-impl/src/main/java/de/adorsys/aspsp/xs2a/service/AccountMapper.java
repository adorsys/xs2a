package de.adorsys.aspsp.xs2a.service;

import de.adorsys.aspsp.xs2a.domain.AccountDetails;
import de.adorsys.aspsp.xs2a.domain.AccountReport;
import de.adorsys.aspsp.xs2a.domain.Balances;
import de.adorsys.aspsp.xs2a.domain.CashAccountType;
import de.adorsys.aspsp.xs2a.domain.Links;
import de.adorsys.aspsp.xs2a.domain.SingleBalance;
import de.adorsys.aspsp.xs2a.domain.code.BankTransactionCode;
import de.adorsys.aspsp.xs2a.domain.code.PurposeCode;
import de.adorsys.aspsp.xs2a.spi.domain.account.AccountBalance;
import de.adorsys.aspsp.xs2a.spi.domain.account.AccountReference;
import de.adorsys.aspsp.xs2a.spi.domain.account.AccountType;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountDetails;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiBalances;
import de.adorsys.aspsp.xs2a.spi.domain.account.Transaction;
import de.adorsys.aspsp.xs2a.spi.domain.common.Amount;

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
                    mapAccountType(accountDetail.getCashAccountType()),
                    accountDetail.getBic(),
                    mapSpiBalances(accountDetail.getBalances()),
                    new Links()
                )
            )
            .orElse(null);

    }

    private CashAccountType mapAccountType(AccountType cashAccountType) {
        return CashAccountType.valueOf(cashAccountType.name());
    }

    Balances mapSpiBalances(SpiBalances spiBalances) {
        return Optional.ofNullable(spiBalances)
            .map(b ->  {
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

    private SingleBalance mapSingleBalances(AccountBalance accountBalance) {
        return Optional.ofNullable(accountBalance)
            .map(b -> {
                SingleBalance singleBalance = new SingleBalance();
                singleBalance.setAmount(mapSpiAmount(b.getAmount()));
                singleBalance.setDate(b.getDate());
                singleBalance.setLastActionDateTime(b.getLastActionDateTime());
                return singleBalance;
            })
            .orElse(null);
    }

    private de.adorsys.aspsp.xs2a.domain.Amount mapSpiAmount(Amount spiAmount) {
        return Optional.ofNullable(spiAmount)
            .map(a -> {
                de.adorsys.aspsp.xs2a.domain.Amount amount = new de.adorsys.aspsp.xs2a.domain.Amount();
                amount.setContent(a.getContent());
                amount.setCurrency(a.getCurrency());
                return amount;
            })
            .orElse(null);
    }

    public AccountReport mapAccountReport(List<Transaction> transactions) {
        if (transactions == null) {
            return null;
        }

        de.adorsys.aspsp.xs2a.domain.Transaction[] booked = transactions
            .stream()
            .filter(transaction -> transaction.getBookingDate() != null)
            .map(this::mapSpiTransaction)
            .toArray(de.adorsys.aspsp.xs2a.domain.Transaction[]::new);

        de.adorsys.aspsp.xs2a.domain.Transaction[] pending = transactions
            .stream()
            .filter(transaction -> transaction.getBookingDate() == null)
            .map(this::mapSpiTransaction)
            .toArray(de.adorsys.aspsp.xs2a.domain.Transaction[]::new);

        return new AccountReport(booked, pending, new Links());
    }

    private de.adorsys.aspsp.xs2a.domain.Transaction mapSpiTransaction(Transaction spiTransaction) {
        return Optional.ofNullable(spiTransaction)
            .map(t -> {
                de.adorsys.aspsp.xs2a.domain.Transaction transaction = new de.adorsys.aspsp.xs2a.domain.Transaction();
                transaction.setAmount(mapSpiAmount(t.getAmount()));
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

    private de.adorsys.aspsp.xs2a.domain.AccountReference mapSpiAccountReference(AccountReference spiAccountReference) {
        return Optional.ofNullable(spiAccountReference)
            .map(ar -> {
                de.adorsys.aspsp.xs2a.domain.AccountReference accountReference = new de.adorsys.aspsp.xs2a.domain.AccountReference();
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
