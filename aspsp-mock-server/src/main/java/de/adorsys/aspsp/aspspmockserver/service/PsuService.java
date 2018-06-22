package de.adorsys.aspsp.aspspmockserver.service;

import de.adorsys.aspsp.aspspmockserver.repository.PsuRepository;
import de.adorsys.aspsp.xs2a.spi.domain.psu.Psu;
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

    public String createPsuAndReturnId(Psu psu) {
        return psu.isValid()
                   ? psuRepository.save(psu).getId()
                   : null;
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
