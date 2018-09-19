/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.aspsp.aspspmockserver.service;

import de.adorsys.aspsp.aspspmockserver.keycloak.KeycloakService;
import de.adorsys.aspsp.aspspmockserver.repository.PsuRepository;
import de.adorsys.aspsp.xs2a.spi.domain.psu.Psu;
import de.adorsys.aspsp.xs2a.spi.domain.psu.SpiScaMethod;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PsuService {
    private final PsuRepository psuRepository;
    private final KeycloakService keycloakService;

    /**
     * Checks psu for validity, saves it to DB and register it to Keycloak
     *
     * @param psu PSU to be saved
     * @return a string representation of ASPSP identifier for saved PSU
     */
    public String createPsuAndReturnId(Psu psu) {
        if (!psu.isValid()) {
            log.error("Psu: {} is invalid", psu.getPsuId());
            return null;
        } else if (psuRepository.findByPsuId(psu.getPsuId()).isPresent()) {
            log.error("Psu with psuId: {} is already exist", psu.getPsuId());
            return null;
        } else if (!keycloakService.registerClient(psu.getPsuId(), psu.getPassword(), psu.getEmail())) {
            log.error("Can't register Psu: {} in Keycloak", psu.getPsuId());
            return null;
        }
        return psuRepository.save(psu)
                   .getAspspPsuId();
    }

    /**
     * Returns PSU by its primary ASPSP identifier
     *
     * @param psuId String representation of ASPSP identifier for specific PSU
     * @return PSU
     */
    public Optional<Psu> getPsuByPsuId(String psuId) {
        return psuRepository.findByPsuId(psuId);
    }

    /**
     * Returns a complete list of all PSUs at current ASPSP
     *
     * @return list of PSU
     */
    public List<Psu> getAllPsuList() {
        return psuRepository.findAll();
    }

    /**
     * Removes PSU for ASPSP by its ASPSP primary identifier
     *
     * @param psuId String representation of ASPSP identifier for specific PSU
     * @return boolean representation of successful deletion(true) or its failure(false)
     */
    public boolean deletePsuByPsuId(String psuId) {
        if (StringUtils.isNotBlank(psuId) && psuRepository.exists(psuId)) {
            psuRepository.deleteByPsuId(psuId);
            return true;
        }
        return false;
    }

    /**
     * Returns a list of allowed products for certain PSU by its ASPSP primary identifier
     *
     * @param iban String representation of iban of PSU`s account
     * @return list of allowed products
     */
    public List<String> getAllowedPaymentProducts(String iban) {
        return psuRepository.findPsuByAccountDetailsList_Iban(iban)
                   .map(Psu::getPermittedPaymentProducts)
                   .orElse(null);
    }

    /**
     * Adds an allowed payment product to corresponding PSU`s list
     *
     * @param psuId   String representation of ASPSP identifier for specific PSU
     * @param product String representation of product to be added
     */
    public void addAllowedProduct(String psuId, String product) {
        Psu psu = getPsuByPsuId(psuId).orElse(null);
        if (psu != null && psu.isValid()) {
            List<String> allowedProducts = psu.getPermittedPaymentProducts();
            if (!allowedProducts.contains(product)) {
                allowedProducts.add(product);
                psu.setPermittedPaymentProducts(allowedProducts);
                psuRepository.save(psu);

            }
        }
    }

    /**
     * Returns a List of SCA methods that could be applied to named PSU
     *
     * @param psuId PSU id
     * @return list of SCA methods
     */
    public List<SpiScaMethod> getScaMethods(String psuId) {
        return psuRepository.findByPsuId(psuId)
                   .map(Psu::getScaMethods)
                   .orElse(null);
    }

    /**
     * Updates allowed PSU`s SCA methods Set
     *
     * @param psuId PSU id
     * @param scaMethods list of SCA methods
     */
    public void updateScaMethods(String psuId, List<SpiScaMethod> scaMethods) {
        psuRepository.findByPsuId(psuId)
            .map(p -> {
                p.setScaMethods(scaMethods);
                return psuRepository.save(p);
            });
    }
}
