package de.adorsys.psd2.consent.repository;

import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.repository.CrudRepository;

import de.adorsys.psd2.consent.domain.TppInfoEntity;

public interface TppInfoRepository extends CrudRepository<TppInfoEntity, Long> {

    Optional<TppInfoEntity> findFirstByAuthorisationNumberAndInstanceIdOrderByIdDesc(@NotNull String authorisationNumber, @NotNull String instanceId);
}
