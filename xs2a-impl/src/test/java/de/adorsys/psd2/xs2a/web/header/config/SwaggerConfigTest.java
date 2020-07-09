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
