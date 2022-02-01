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

package de.adorsys.psd2.consent.service.psu.util;

import de.adorsys.psd2.xs2a.core.pagination.data.PageRequestParameters;
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
    void getPageable_pageRequestParametersIsNuLL() {
        // Given
        Pageable expected = Pageable.unpaged();

        // When
        Pageable actual = pageRequestBuilder.getPageable(null);

        // Then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void getPageable_pageRequestParametersIsValid() {
        // Given
        PageRequest expected = PageRequest.of(5, 28);

        // When
        Pageable actual = pageRequestBuilder.getPageable(new PageRequestParameters(5,28));

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
