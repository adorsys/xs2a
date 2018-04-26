package de.adorsys.aspsp.aspspmockserver.repository;

import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountConsent;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
@Profile({"mongo", "fongo"})
public interface ConsentRepository extends MongoRepository<SpiAccountConsent, String> {
}
