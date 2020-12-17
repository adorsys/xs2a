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
