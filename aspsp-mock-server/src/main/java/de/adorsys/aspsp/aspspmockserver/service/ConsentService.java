package de.adorsys.aspsp.aspspmockserver.service;

import de.adorsys.aspsp.aspspmockserver.repository.ConsentRepository;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountConsent;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Service
public class ConsentService {
    private ConsentRepository consentRepository;

    public Optional<String> createConsentAndReturnId(SpiAccountConsent consent) {
        return Optional.ofNullable(consentRepository.save(consent)).map(SpiAccountConsent::getId);
    }

    public Optional<SpiAccountConsent> getConsent(String consentId) {
        return Optional.ofNullable(consentRepository.findOne(consentId));
    }

    public List<SpiAccountConsent> getAllConsents() {
        return consentRepository.findAll();
    }

    public boolean deleteConsentById(String id) {
        if (id != null && consentRepository.exists(id)) {
            consentRepository.delete(id);
            return true;
        }
        return false;
    }
}
