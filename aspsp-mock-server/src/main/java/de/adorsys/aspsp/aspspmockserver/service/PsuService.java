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

import de.adorsys.aspsp.aspspmockserver.repository.PsuRepository;
import de.adorsys.aspsp.xs2a.spi.domain.psu.Psu;
import org.apache.commons.lang3.StringUtils;
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

    /**
     * Checks psu for validity and saves it to DB
     *
     * @param psu PSU to be saved
     * @return a string representation of ASPSP identifier for saved PSU
     */
    public String createPsuAndReturnId(Psu psu) {
        return psu.isValid()
                   ? psuRepository.save(psu).getId()
                   : null;
    }

    /**
     * Returns PSU by its primary ASPSP identifier
     *
     * @param psuId String representation of ASPSP identifier for specific PSU
     * @return PSU
     */
    public Optional<Psu> getPsuById(String psuId) {
        return Optional.ofNullable(psuRepository.findOne(psuId));
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
    public boolean deletePsuById(String psuId) {
        if (StringUtils.isNotBlank(psuId) && psuRepository.exists(psuId)) {
            psuRepository.delete(psuId);
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
     * @param psuId      String representation of ASPSP identifier for specific PSU
     * @param product String representation of product to be added
     */
    public void addAllowedProduct(String psuId, String product) {
        Psu psu = getPsuById(psuId).orElse(null);
        if (psu != null && psu.isValid()) {
            List<String> allowedProducts = psu.getPermittedPaymentProducts();
            if (!allowedProducts.contains(product)) {
                allowedProducts.add(product);
                psu.setPermittedPaymentProducts(allowedProducts);
                psuRepository.save(psu);

            }
        }

    }
}
