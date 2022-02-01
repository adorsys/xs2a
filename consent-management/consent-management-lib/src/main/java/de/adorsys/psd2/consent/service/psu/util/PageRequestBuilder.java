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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class PageRequestBuilder {
    @Value("${cms.defaultPageIndex:0}")
    private int defaultPageIndex;
    @Value("${cms.defaultItemsPerPage:20}")
    private int defaultItemsPerPage;

    public Pageable getPageable(Integer pageIndex, Integer itemsPerPage) {
        if (pageIndex == null && itemsPerPage == null) {
            return Pageable.unpaged();
        }
        return PageRequest.of(getValueOrDefault(pageIndex, defaultPageIndex),
            getValueOrDefault(itemsPerPage, defaultItemsPerPage));
    }

    public Pageable getPageable(PageRequestParameters pageRequestParameters) {
        if (pageRequestParameters == null) {
            return Pageable.unpaged();
        }
        return getPageable(pageRequestParameters.getPageIndex(), pageRequestParameters.getItemsPerPage());
    }

    private int getValueOrDefault(Integer value, int defaultValue) {
        if (value == null || value < 0) {
            return defaultValue;
        }
        return value;
    }
}
