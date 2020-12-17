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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class PageRequestBuilderTest {
    private static final int DEFAULT_PAGE_INDEX = 0;
    private static final int DEFAULT_ITEMS_PER_PAGE = 50;

    @InjectMocks
    private PageRequestBuilder pageRequestBuilder;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(pageRequestBuilder,
                                     "defaultPageIndex",
                                     DEFAULT_PAGE_INDEX);
        ReflectionTestUtils.setField(pageRequestBuilder,
                                     "defaultItemsPerPage",
                                     DEFAULT_ITEMS_PER_PAGE);
    }

    @Test
    void getPageable_nonDefault() {
        // Given
        PageRequest expected = PageRequest.of(5, 28);

        // When
        Pageable actual = pageRequestBuilder.getPageable(5, 28);

        // Then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void getPageable_defaults() {
        // Given
        // When
        Pageable actual = pageRequestBuilder.getPageable(null, null);

        // Then
        assertThat(actual).isEqualTo(Pageable.unpaged());
    }

    @Test
    void getPageable_negativeValues() {
        Pageable actual = pageRequestBuilder.getPageable(-1, -1);

        PageRequest expected = PageRequest.of(DEFAULT_PAGE_INDEX, DEFAULT_ITEMS_PER_PAGE);
        assertThat(actual).isEqualTo(expected);
    }
}
