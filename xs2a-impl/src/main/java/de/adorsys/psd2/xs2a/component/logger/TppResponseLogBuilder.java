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
