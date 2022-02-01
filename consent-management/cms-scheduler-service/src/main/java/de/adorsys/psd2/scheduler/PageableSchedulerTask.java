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

package de.adorsys.psd2.scheduler;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public abstract class PageableSchedulerTask {

    @Value("${xs2a.cms.scheduler.processing.page-size:100}")
    protected int pageSize;

    /**
     * Based on amount of items calculates page count and executes `runPageable` method with {@link Pageable} parameter
     *
     * @param totalItems total items
     */
    protected void execute(long totalItems) {
        int totalPages = (int) ((totalItems + pageSize - 1) / pageSize);
        for (int page = 0; page < totalPages; page++) {
            Pageable pageable = PageRequest.of(page, pageSize);

            executePageable(pageable);
        }
    }

    protected abstract void executePageable(Pageable pageable);
}
