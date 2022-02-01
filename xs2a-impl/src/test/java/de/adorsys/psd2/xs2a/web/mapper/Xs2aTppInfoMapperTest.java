/*
 * Copyright 2018-2022 adorsys GmbH & Co KG
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version. This program is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 *
 * This project is also available under a separate commercial license. You can
 * contact us at psd2@adorsys.com.
 */

package de.adorsys.psd2.xs2a.web.mapper;

import de.adorsys.psd2.validator.certificate.util.TppCertificateData;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.core.tpp.TppRole;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {Xs2aTppInfoMapperImpl.class})
class Xs2aTppInfoMapperTest {
    @Autowired
    private Xs2aTppInfoMapper xs2aTppInfoMapper;
    private JsonReader jsonReader = new JsonReader();

    @Test
    void mapToTppInfo() {
        //Given
        TppCertificateData tppCertificateData = jsonReader.getObjectFromFile("json/service/mapper/tpp-certificate-data.json", TppCertificateData.class);
        TppInfo tppInfoExpected = jsonReader.getObjectFromFile("json/service/mapper/tpp-info.json", TppInfo.class);
        //When
        TppInfo tppInfo = xs2aTppInfoMapper.mapToTppInfo(tppCertificateData);
        //Then
        assertEquals(tppInfoExpected, tppInfo);
    }

    @Test
    void mapToTppInfo_nullTppInfoEntity() {
        TppInfo actual = xs2aTppInfoMapper.mapToTppInfo(null);
        assertNull(actual);
    }

    @Test
    void mapToTppRoles() {
        //Given
        String header = "AISP, PISP, PIISP, ASPSP, UNKNOWN_ROLE";
        List<String> rolesList = Arrays.asList(header.split(","));
        List<TppRole> rolesExpected = Arrays.asList(TppRole.AISP, TppRole.PISP, TppRole.PIISP, TppRole.ASPSP);
        //When
        List<TppRole> roles = xs2aTppInfoMapper.mapToTppRoles(rolesList);
        //Then
        assertEquals(rolesExpected, roles);
    }
}
