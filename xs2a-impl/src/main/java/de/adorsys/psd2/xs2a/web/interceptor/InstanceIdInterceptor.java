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

package de.adorsys.psd2.xs2a.web.interceptor;

import de.adorsys.psd2.xs2a.domain.InstanceIdRequestHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@Component
@RequiredArgsConstructor
public class InstanceIdInterceptor extends HandlerInterceptorAdapter {
    private static final String INSTANCE_ID_HEADER = "Instance-ID";
    private final InstanceIdRequestHolder instanceIdRequestHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String instanceIdHeader = request.getHeader(INSTANCE_ID_HEADER);
        if (StringUtils.isNotBlank(instanceIdHeader)) {
            instanceIdRequestHolder.setInstanceId(instanceIdHeader);
            log.info("Header 'instance-id' is applied : {}", instanceIdHeader);
        }
        return true;
    }
}
