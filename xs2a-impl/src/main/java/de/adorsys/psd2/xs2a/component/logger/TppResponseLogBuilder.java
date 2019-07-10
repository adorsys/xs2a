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

package de.adorsys.psd2.xs2a.component.logger;

import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.web.validator.constants.Xs2aHeaderConstant;

import javax.servlet.http.HttpServletResponse;

public class TppResponseLogBuilder extends TppLogger.TppLogBuilder<TppResponseLogBuilder> {
    private static final String TPP_ID = "TPP ID";
    private static final String RESPONSE_STATUS = "Status";

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

    @Override
    protected String getXRequestIdValue() {
        return response.getHeader(Xs2aHeaderConstant.X_REQUEST_ID);
    }

    @Override
    protected TppResponseLogBuilder getThis() {
        return this;
    }
}
