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
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountDetails;
import de.adorsys.aspsp.xs2a.spi.domain.psu.Psu;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PsuServiceTest {
    private static final String PSU_ID = "ec818c89-4346-4f16-b5c8-d781b040200c";
    private static final String PSU_ID_1 = "zz99999-9999-9999-9999-999999999999";
    private static final String WRONG_PSU_ID = "Wrong psu id";
    private static final String E_MAIL = "info@adorsys.ua";
    private static final String PSU_NAME = "aspsp";
    private static final String NONEXISTEN_PSU_NAME = "nonexisten";
    private static final String PASSWORD = "zzz";
    private static final String WRONG_E_MAIL = "wrong e-mail";
    private static final Currency EUR = Currency.getInstance("EUR");
    private static final String ACCOUNT_ID = "ACCOUNT1-0000-0000-0000-a000q000000t";
    private static final String IBAN = "DE123456789";
    private static final String WRONG_IBAN = "wrong_iban";
    private static final String CORRECT_PAYMENT_PRODUCT = "sepa-credit-transfers";

    @InjectMocks
    private PsuService psuService;

    @Mock
    private PsuRepository psuRepository;
    @Mock
    private KeycloakService keycloakService;

    @Before
    public void setUp() {
        //findAll
        when(psuRepository.findAll()).thenReturn(Collections.singletonList(getPsu(PSU_ID, E_MAIL, PSU_NAME, getDetails(false), getProducts())));

        //findOne
        when(psuRepository.findOne(PSU_ID)).thenReturn(getPsu(PSU_ID, E_MAIL, PSU_NAME,  getDetails(false), getProducts()));
        when(psuRepository.findOne(PSU_ID_1)).thenReturn(getPsu(PSU_ID_1, E_MAIL, PSU_NAME, getDetails(false), getProducts()));
        when(psuRepository.findOne(WRONG_PSU_ID)).thenReturn(null);

        //findByIban
        when(psuRepository.findPsuByAccountDetailsList_Iban(IBAN)).thenReturn(Optional.of(getPsu(PSU_ID, E_MAIL, PSU_NAME, getDetails(false), getProducts())));
        when(psuRepository.findPsuByAccountDetailsList_Iban(WRONG_IBAN)).thenReturn(Optional.empty());

        // find Psu by name
        when(psuRepository.findPsuByName(NONEXISTEN_PSU_NAME)).thenReturn(Optional.empty());
        when(keycloakService.registerClient(NONEXISTEN_PSU_NAME, PASSWORD, E_MAIL)).thenReturn(true);

        //exists
        when(psuRepository.exists(PSU_ID)).thenReturn(true);
        when(psuRepository.exists(WRONG_PSU_ID)).thenReturn(false);
        doNothing().when(psuRepository).delete(PSU_ID);

        //save
        when(psuRepository.save(getPsu(null, E_MAIL, PSU_NAME, getDetails(false), getProducts())))
            .thenReturn(getPsu(PSU_ID, E_MAIL, PSU_NAME, getDetails(false), getProducts()));
        when(psuRepository.save(getPsu(null, E_MAIL, NONEXISTEN_PSU_NAME, getDetails(false), getProducts())))
            .thenReturn(getPsu(PSU_ID, E_MAIL, NONEXISTEN_PSU_NAME, getDetails(false), getProducts()));
        when(psuRepository.save(getPsu(PSU_ID, E_MAIL, PSU_NAME, getDetails(false), getProductsExt())))
            .thenReturn(getPsu(PSU_ID, E_MAIL, PSU_NAME, getDetails(false), getProductsExt()));
        when(psuRepository.save(getPsu(PSU_ID_1, E_MAIL, PSU_NAME, getDetails(true), getProductsExt())))
            .thenReturn(getPsu(PSU_ID_1, E_MAIL, PSU_NAME, getDetails(false), getProducts()));
    }

    @Test
    public void createPsu_Success() {
        //When
        String actualResult = psuService.createPsuAndReturnId(getPsu(null, E_MAIL, NONEXISTEN_PSU_NAME, getDetails(false), getProducts()));

        //Then
        assertThat(actualResult).isEqualTo(PSU_ID);
    }

    @Test
    public void createPsu_Failure_wrong_email() {
        String actualResult = psuService.createPsuAndReturnId(getPsu(null, WRONG_E_MAIL, PSU_NAME, getDetails(false), getProducts()));

        //Then
        assertThat(actualResult).isEqualTo(null);
    }

    @Test
    public void createPsu_Failure_emptyDetails() {
        String actualResult = psuService.createPsuAndReturnId(getPsu(null, E_MAIL, PSU_NAME, getDetails(true), getProducts()));

        //Then
        assertThat(actualResult).isEqualTo(null);
    }

    @Test
    public void createPsu_Failure_emptyProducts() {
        String actualResult = psuService.createPsuAndReturnId(getPsu(null, E_MAIL, PSU_NAME, getDetails(false), Collections.emptyList()));

        //Then
        assertThat(actualResult).isEqualTo(null);
    }

    @Test
    public void readAllPsuList_Success() {
        //When
        List<Psu> actualResult = psuService.getAllPsuList();

        //Then
        assertThat(actualResult).isEqualTo(Collections.singletonList(getPsu(PSU_ID, E_MAIL, PSU_NAME, getDetails(false), getProducts())));
    }

    @Test
    public void readPsuById_Success() {
        //When
        Optional<Psu> actualResult = psuService.getPsuById(PSU_ID);

        //Then
        assertThat(actualResult.isPresent()).isTrue();
        assertThat(actualResult.get().getId()).isEqualTo(PSU_ID);
    }

    @Test
    public void readPsuById_Success1() {
        //When
        Optional<Psu> actualResult = psuService.getPsuById(PSU_ID_1);

        //Then
        assertThat(actualResult.isPresent()).isTrue();
        assertThat(actualResult.get().getId()).isEqualTo(PSU_ID_1);
    }

    @Test
    public void readPsuById_Failure_wrong_id() {
        //When
        Optional<Psu> actualResult = psuService.getPsuById(WRONG_PSU_ID);

        //Then
        assertThat(actualResult.isPresent()).isFalse();
    }

    @Test
    public void readPaymentProductsById_Success() {
        //When
        List<String> actualResult = psuService.getAllowedPaymentProducts(IBAN);

        //Then
        assertThat(actualResult).isEqualTo(getProducts());
    }

    @Test
    public void readPaymentProductsById_Failure_wrong_id() {
        //When
        List<String> actualResult = psuService.getAllowedPaymentProducts(WRONG_IBAN);

        //Then
        assertThat(actualResult).isEqualTo(null);
    }

    @Test
    public void deletePsu_Success() {
        //When
        boolean actualResult = psuService.deletePsuById(PSU_ID);

        //Then
        assertThat(actualResult).isTrue();
    }

    @Test
    public void deletePsu_Failure_wrong_id() {
        //When
        boolean actualResult = psuService.deletePsuById(WRONG_PSU_ID);

        //Then
        assertThat(actualResult).isFalse();
    }


    private List<String> getProducts() {
        return Collections.singletonList(CORRECT_PAYMENT_PRODUCT);
    }

    private List<String> getProductsExt() {
        List<String> products = new ArrayList<>(getProducts());
        products.add("new product");
        return products;
    }

    private Psu getPsu(String psuId, String email, String name, List<SpiAccountDetails> details, List<String> products) {
        return new Psu(psuId, email, name, PASSWORD, details, products);
    }

    private List<SpiAccountDetails> getDetails(boolean isEmpty) {
        return isEmpty
                   ? Collections.emptyList()
                   : Collections.singletonList(new SpiAccountDetails(ACCOUNT_ID, IBAN, null, null, null, null, EUR, "Alfred", null, null, null, Collections.emptyList()));

    }
}
