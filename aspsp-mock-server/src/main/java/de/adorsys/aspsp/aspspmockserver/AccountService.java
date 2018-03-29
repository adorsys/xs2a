package de.adorsys.aspsp.aspspmockserver;

import de.adorsys.aspsp.aspspmockserver.repository.AccountRepository;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountDetails;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AccountService {
    private AccountRepository accountRepository;

    @Autowired
    public AccountService(AccountRepository accountRepository) {this.accountRepository = accountRepository;}

    public SpiAccountDetails addAccount(SpiAccountDetails accountDetails) {
        return accountRepository.save(accountDetails);
    }

    public List<SpiAccountDetails> getAllAccounts() {
        return accountRepository.findAll();
    }

    public Optional<SpiAccountDetails> getAccount(String id) {
        return Optional.ofNullable(accountRepository.findOne(id));
    }
}
