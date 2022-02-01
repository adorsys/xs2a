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

package de.adorsys.psd2.xs2a.web.filter;

import de.adorsys.psd2.xs2a.web.Xs2aEndpointChecker;
import de.adorsys.psd2.xs2a.web.error.TppErrorMessageWriter;

import javax.servlet.http.HttpServletRequest;

/**
 * Abstract filter that will be executed only once and will be applied only to XS2A endpoints.
 */
public abstract class AbstractXs2aFilter extends GlobalAbstractExceptionFilter {
    private final Xs2aEndpointChecker xs2aEndpointChecker;

    protected AbstractXs2aFilter(TppErrorMessageWriter tppErrorMessageWriter, Xs2aEndpointChecker xs2aEndpointChecker) {
        super(tppErrorMessageWriter);
        this.xs2aEndpointChecker = xs2aEndpointChecker;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !xs2aEndpointChecker.isXs2aEndpoint(request);
    }
}
