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
