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

package de.adorsys.psd2.consent.service.psu.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class PageRequestBuilderTest {
    public static final Integer PAGE_INDEX = 5;
    public static final Integer ITEMS_PER_PAGE = 28;

    @InjectMocks
    private PageRequestBuilder pageRequestBuilder;

    @Test
    void getPageParams_nonDefault() {
        // Given
        PageRequest expected = PageRequest.of(PAGE_INDEX, ITEMS_PER_PAGE);

        // When
        PageRequest actual = pageRequestBuilder.getPageParams(PAGE_INDEX, ITEMS_PER_PAGE);

        // Then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void getPageParams_defaults() {
        // Given
        PageRequest expected = PageRequest.of(0, 50);

        ReflectionTestUtils.setField(pageRequestBuilder,
                                     "defaultPageIndex",
                                     0);

        ReflectionTestUtils.setField(pageRequestBuilder,
                                     "defaultItemsPerPage",
                                     50);

        // When
        PageRequest actual = pageRequestBuilder.getPageParams(null, null);

        // Then
        assertThat(actual).isEqualTo(expected);
    }
}
