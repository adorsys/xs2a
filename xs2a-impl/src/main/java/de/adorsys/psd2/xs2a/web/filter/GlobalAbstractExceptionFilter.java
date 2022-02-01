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

import de.adorsys.psd2.xs2a.web.error.TppErrorMessageWriter;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public abstract class GlobalAbstractExceptionFilter extends OncePerRequestFilter {
    private final TppErrorMessageWriter tppErrorMessageWriter;

    protected GlobalAbstractExceptionFilter(TppErrorMessageWriter tppErrorMessageWriter) {
        this.tppErrorMessageWriter = tppErrorMessageWriter;
    }

    //This method is final to prevent it's overriding in child classes. Please override doFilterInternalCustom instead.
    @Override
    protected final void doFilterInternal(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull FilterChain filterChain) throws ServletException, IOException {
        try {
            doFilterInternalCustom(request, response, filterChain);
        } catch (ResourceAccessException e) {
            tppErrorMessageWriter.writeServiceUnavailableError(response, e.getMessage());
        }
    }

    protected abstract void doFilterInternalCustom(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException;
}
