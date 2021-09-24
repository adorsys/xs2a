/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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

package de.adorsys.psd2.consent.service.mapper;

import de.adorsys.psd2.consent.api.ais.AdditionalAccountInformationType;
import de.adorsys.psd2.consent.domain.account.AspspAccountAccess;
import de.adorsys.psd2.consent.domain.account.TppAccountAccess;
import de.adorsys.psd2.core.data.AccountAccess;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AccessMapperTest {
    AccessMapper accessMapper = new AccessMapper();
    JsonReader jsonReader = new JsonReader();

    @Test
    void mapTppAccessesToAccountAccess() {
        List<TppAccountAccess> tppAccountAccesses = jsonReader.getListFromFile("json/service/mapper/access-mapper/tpp-account-accesses-additional-information.json", TppAccountAccess.class);
        AccountAccess actual = accessMapper.mapTppAccessesToAccountAccess(tppAccountAccesses, AdditionalAccountInformationType.DEDICATED_ACCOUNTS, AdditionalAccountInformationType.DEDICATED_ACCOUNTS);

        AccountAccess expected = jsonReader.getObjectFromFile("json/service/mapper/access-mapper/account-access-for-tpp-additional-information.json", AccountAccess.class);
        assertEquals(expected, actual);
    }

    @Test
    void mapToTppAccountAccess() {
        AccountAccess accountAccess = jsonReader.getObjectFromFile("json/service/mapper/access-mapper/account-access-for-tpp-additional-information.json", AccountAccess.class);
        List<TppAccountAccess> actual = accessMapper.mapToTppAccountAccess(null, accountAccess);

        List<TppAccountAccess> expected = jsonReader.getListFromFile("json/service/mapper/access-mapper/tpp-account-accesses-additional-information.json", TppAccountAccess.class);
        assertEquals(expected, actual);
    }

    @Test
    void mapAspspAccessesToAccountAccess() {
        List<AspspAccountAccess> aspspAccountAccesses = jsonReader.getListFromFile("json/service/mapper/access-mapper/aspsp-account-accesses-additional-information.json", AspspAccountAccess.class);
        AccountAccess actual = accessMapper.mapAspspAccessesToAccountAccess(aspspAccountAccesses, AdditionalAccountInformationType.DEDICATED_ACCOUNTS, AdditionalAccountInformationType.DEDICATED_ACCOUNTS);

        AccountAccess expected = jsonReader.getObjectFromFile("json/service/mapper/access-mapper/account-access-for-aspsp-additional-information.json", AccountAccess.class);
        assertEquals(expected, actual);
    }

    @Test
    void mapToAspspAccountAccess() {
        AccountAccess accountAccess = jsonReader.getObjectFromFile("json/service/mapper/access-mapper/account-access-for-aspsp.json", AccountAccess.class);
        List<AspspAccountAccess> actual = accessMapper.mapToAspspAccountAccess(null, accountAccess);

        List<AspspAccountAccess> expected = jsonReader.getListFromFile("json/service/mapper/access-mapper/aspsp-account-accesses.json", AspspAccountAccess.class);
        assertEquals(expected, actual);
    }

    @Test
    void mapToAspspAccountAccess_additionalInformation() {
        AccountAccess accountAccess = jsonReader.getObjectFromFile("json/service/mapper/access-mapper/account-access-for-aspsp-additional-information.json", AccountAccess.class);
        List<AspspAccountAccess> actual = accessMapper.mapToAspspAccountAccess(null, accountAccess);

        List<AspspAccountAccess> expected = jsonReader.getListFromFile("json/service/mapper/access-mapper/aspsp-account-accesses-additional-information.json", AspspAccountAccess.class);
        assertEquals(expected, actual);
    }

    @Test
    void mapToAspspAccountAccess_accountReference() {
        AccountReference accountReference = jsonReader.getObjectFromFile("json/service/mapper/access-mapper/account-reference.json", AccountReference.class);
        AspspAccountAccess expected = jsonReader.getObjectFromFile("json/service/mapper/access-mapper/aspsp-account-accesses-account.json", AspspAccountAccess.class);

        AspspAccountAccess actual = accessMapper.mapToAspspAccountAccess(null, accountReference);

        assertEquals(expected, actual);
    }

    @Test
    void mapToAccountReference() {
        AspspAccountAccess aspspAccountAccess = jsonReader.getObjectFromFile("json/service/mapper/access-mapper/aspsp-account-accesses-account.json", AspspAccountAccess.class);
        AccountReference expected = jsonReader.getObjectFromFile("json/service/mapper/access-mapper/account-reference.json", AccountReference.class);

        AccountReference actual = accessMapper.mapToAccountReference(aspspAccountAccess);

        assertEquals(expected, actual);
    }
}
