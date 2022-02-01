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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PageableSchedulerTaskTest {

    private static int COUNT = 0;
    private static final int PAGE_SIZE = 100;

    private PageableSchedulerTask pageableSchedulerTask;

    @BeforeEach
    void setUp() {
        pageableSchedulerTask = new PageableSchedulerTask() {
            @Override
            protected void executePageable(Pageable pageable) {
                COUNT++;
            }
        };
        ReflectionTestUtils.setField(pageableSchedulerTask, "pageSize", PAGE_SIZE);
    }

    @Test
    void calculatePageCount() {
        validateRunPageableMethodCountExecution(0, 0);
        validateRunPageableMethodCountExecution(1, 1);
        validateRunPageableMethodCountExecution(1, 100);
        validateRunPageableMethodCountExecution(2, 101);
    }

    private void validateRunPageableMethodCountExecution(int expectedCount, int totalItems) {
        pageableSchedulerTask.execute(totalItems);
        assertEquals(expectedCount, COUNT);
        resetCount();
    }

    private void resetCount() {
        COUNT = 0;
    }
}
