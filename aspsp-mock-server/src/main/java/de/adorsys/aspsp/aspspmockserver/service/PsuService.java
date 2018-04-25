package de.adorsys.aspsp.aspspmockserver.service;

import de.adorsys.aspsp.aspspmockserver.repository.PsuRepository;
import de.adorsys.aspsp.xs2a.spi.domain.Psu;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PsuService {
    private PsuRepository psuRepository;

    @Autowired
    public PsuService(PsuRepository psuRepository) {
        this.psuRepository = psuRepository;
    }

    public String createPsuAndReturnId(List<SpiAccountDetails> detailsList) {
        Psu psu = new Psu("",detailsList);
        //psu.setAccountDetailsList(detailsList);
        psu = psuRepository.save(psu);

        return psu.getId();
    }

    public Optional<Psu> getPsuById(String id) {
        return Optional.ofNullable(psuRepository.findOne(id));
    }

    public List<Psu> getAllPsuList() {
        return psuRepository.findAll();
    }

    public boolean deletePsuById(String id) {
        if (id != null && psuRepository.exists(id)) {
            psuRepository.delete(id);
            return true;
        }
        return false;
    }
}
