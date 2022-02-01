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
import org.jetbrains.annotations.Nullable;

import javax.servlet.http.HttpServletResponse;

public class TppResponseLogBuilder extends TppLogger.TppLogBuilder<TppResponseLogBuilder> {
    private static final String TPP_ID = "TPP ID";
    private static final String RESPONSE_STATUS = "Status";
    private static final String REDIRECT_ID = "Redirect-ID";

    private HttpServletResponse response;

    TppResponseLogBuilder(HttpServletResponse httpServletResponse) {
        super(TppLogType.RESPONSE);
        this.response = httpServletResponse;
    }

    public TppResponseLogBuilder withTpp(TppInfo tppInfo) {
        putLogParameter(TPP_ID, tppInfo.getAuthorisationNumber());
        return this;
    }

    public TppResponseLogBuilder withResponseStatus() {
        putLogParameter(RESPONSE_STATUS, String.valueOf(response.getStatus()));
        return this;
    }

    public TppResponseLogBuilder withOptionalRedirectId(@Nullable String redirectId) {
        if (redirectId != null) {
            putLogParameter(REDIRECT_ID, redirectId);
        }

        return this;
    }

    @Override
    protected TppResponseLogBuilder getThis() {
        return this;
    }
}
