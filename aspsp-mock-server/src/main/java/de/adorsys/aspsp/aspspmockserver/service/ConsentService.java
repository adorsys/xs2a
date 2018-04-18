package de.adorsys.aspsp.aspspmockserver.service;

import de.adorsys.aspsp.aspspmockserver.repository.ConsentRepository;
import de.adorsys.aspsp.aspspmockserver.repository.PsuRepository;
import de.adorsys.aspsp.xs2a.spi.domain.Psu;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountDetails;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiTransactionStatus;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiAccountAccess;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiAccountAccessType;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiConsentStatus;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiCreateConsentRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ConsentService {
    private ConsentRepository consentRepository;
    private PsuRepository psuRepository;

    @Autowired
    public ConsentService(ConsentRepository consentRepository, PsuRepository psuRepository) {
        this.consentRepository = consentRepository;
        this.psuRepository = psuRepository;
    }

    public String createConsentAndReturnId(SpiCreateConsentRequest request, String psuId) {
        SpiAccountAccess access = checkAccess(request.getAccess(), psuId);

        SpiAccountConsent consent = access == null ? null
                                    : consentRepository.save(new SpiAccountConsent(UUID.randomUUID().toString(), access,
        request.isRecurringIndicator(), request.getValidUntil(), request.getFrequencyPerDay(), new Date(),
        SpiTransactionStatus.ACCP, SpiConsentStatus.VALID, true, true));

        return consent != null ? consent.getId() : null;
    }

    public Optional<SpiAccountConsent> getConsent(String id) {
        return Optional.ofNullable(consentRepository.findOne(id));
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

    private SpiAccountAccess checkAccess(SpiAccountAccess access, String psuId) {
        SpiAccountAccess acc = null;
        if (access != null) {
            if (access.getAvailableAccounts() != SpiAccountAccessType.ALL_ACCOUNTS || access.getAllPsd2() != SpiAccountAccessType.ALL_ACCOUNTS) {
                if (psuId != null) {
                    Psu psu = psuRepository.findOne(psuId);
                    if (psu != null) {
                        List<SpiAccountReference> list = new ArrayList<>();
                        for (SpiAccountDetails det : psu.getAccountDetailsList()) {
                            list.add(mapFromSpiAccountDetails(det));
                        }
                        acc = new SpiAccountAccess();
                        acc.setAccounts(list);
                        acc.setBalances(list);
                        acc.setTransactions(list);
                        if (access.getAvailableAccounts() != null) {
                            acc.setAvailableAccounts(SpiAccountAccessType.ALL_ACCOUNTS);
                        } else {
                            acc.setAllPsd2(SpiAccountAccessType.ALL_ACCOUNTS);
                        }
                    }
                }
            }
            if (testAccess(access)) {
                acc = new SpiAccountAccess();
                acc.setAccounts(checkReference(access.getAccounts()));
                acc.setBalances(checkReference(access.getBalances()));
                acc.setTransactions(checkReference(access.getTransactions()));
                if (!testAccess(acc)) {
                    acc = null;
                }
            }
        }

        return acc;
    }

    private boolean testAccess(SpiAccountAccess access) {
        return !access.getAccounts().isEmpty() || !access.getBalances().isEmpty() || !access.getTransactions().isEmpty();
    }

    private List<SpiAccountReference> checkReference(List<SpiAccountReference> accounts) {
        List<SpiAccountReference> result = new ArrayList<>();

        for (SpiAccountReference aR : accounts) {
            Psu psu = psuRepository.findPsuByAccountDetailsList_Iban(aR.getIban());
            if (psu != null) {
                for (SpiAccountDetails det : psu.getAccountDetailsList()) {
                    if (det.getIban().equals(aR.getIban())) {
                        aR = mapFromSpiAccountDetails(det);
                    }
                }
            }
        }
        return result;
    }

    private SpiAccountReference mapFromSpiAccountDetails(SpiAccountDetails details) {
        return new SpiAccountReference(details.getId(), details.getIban(), details.getBban(), details.getPan(), details.getMaskedPan(), details.getMsisdn(), details.getCurrency());
    }
}
