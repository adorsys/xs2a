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

package de.adorsys.psd2.xs2a.web.header.config;

import de.adorsys.psd2.xs2a.web.config.SwaggerConfig;
import de.adorsys.psd2.xs2a.web.config.SwaggerResourceBuilder;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import springfox.documentation.swagger.web.InMemorySwaggerResourcesProvider;
import springfox.documentation.swagger.web.SwaggerResource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {SwaggerConfig.class, SwaggerResourceBuilder.class})
class SwaggerConfigTest {
    private static final String SWAGGER_RESOURCES_PATH = "json/web/config/swagger/swagger-resources.json";
    private final JsonReader jsonReader = new JsonReader();

    @Autowired
    private InMemorySwaggerResourcesProvider swaggerResourcesProvider;
    @Autowired
    private SwaggerConfig swaggerConfig;

    @Test
    void test() {
        //Given
        //When
        List<SwaggerResource> swaggerResources = swaggerConfig.swaggerResourcesProvider(swaggerResourcesProvider).get();
        //Then
        List<SwaggerResource> expected = jsonReader.getListFromFile(SWAGGER_RESOURCES_PATH, SwaggerResource.class);

        for (int i = 0; i < swaggerResources.size(); i++) {
            assertThat(swaggerResources.get(i)).isEqualToComparingFieldByField(expected.get(i));

        }
    }
}
