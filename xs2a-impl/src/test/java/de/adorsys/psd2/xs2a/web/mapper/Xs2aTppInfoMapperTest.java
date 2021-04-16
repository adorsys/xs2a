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
