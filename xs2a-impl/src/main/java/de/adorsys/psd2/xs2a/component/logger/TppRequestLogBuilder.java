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

package de.adorsys.psd2.xs2a.component.logger;

import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;

public class TppRequestLogBuilder extends TppLogger.TppLogBuilder<TppRequestLogBuilder> {
    private static final String TPP_ID = "TPP ID";
    private static final String TPP_IP_ADDRESS = "TPP IP Address";
    private static final String TPP_ROLES = "TPP Roles";
    private static final String TPP_ROLES_SEPARATOR = ",";
    private static final String REQUEST_URI = "URI";

    private HttpServletRequest request;

    TppRequestLogBuilder(HttpServletRequest httpServletRequest) {
        super(TppLogType.REQUEST);
        this.request = httpServletRequest;
    }

    public TppRequestLogBuilder withTpp(TppInfo tppInfo) {
        putLogParameter(TPP_ID, tppInfo.getAuthorisationNumber());
        putLogParameter(TPP_IP_ADDRESS, request.getRemoteAddr());
        putLogParameter(TPP_ROLES, StringUtils.join(tppInfo.getTppRoles(), TPP_ROLES_SEPARATOR));
        return this;
    }

    public TppRequestLogBuilder withRequestUri() {
        putLogParameter(REQUEST_URI, request.getRequestURI());
        return this;
    }

    @Override
    protected TppRequestLogBuilder getThis() {
        return this;
    }
}
