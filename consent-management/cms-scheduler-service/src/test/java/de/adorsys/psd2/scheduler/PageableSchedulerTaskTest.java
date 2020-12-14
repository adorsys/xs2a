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
