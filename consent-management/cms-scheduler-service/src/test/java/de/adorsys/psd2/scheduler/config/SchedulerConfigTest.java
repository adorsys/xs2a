/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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

package de.adorsys.psd2.scheduler.config;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class SchedulerConfigTest {

    @Test
    public void configureTasks() {
        ArgumentCaptor<ThreadPoolTaskScheduler> threadPoolTaskSchedulerCaptor = ArgumentCaptor.forClass(ThreadPoolTaskScheduler.class);
        ScheduledTaskRegistrar scheduledTaskRegistrar = mock(ScheduledTaskRegistrar.class);

        doNothing().when(scheduledTaskRegistrar).setTaskScheduler(threadPoolTaskSchedulerCaptor.capture());

        SchedulerConfig schedulerConfig = new SchedulerConfig();
        ReflectionTestUtils.setField(schedulerConfig, "poolSize", 10);
        schedulerConfig.configureTasks(scheduledTaskRegistrar);

        assertEquals("consent-scheduler-pool", threadPoolTaskSchedulerCaptor.getValue().getThreadNamePrefix());
        assertEquals(10, threadPoolTaskSchedulerCaptor.getValue().getScheduledThreadPoolExecutor().getCorePoolSize());
    }
}
