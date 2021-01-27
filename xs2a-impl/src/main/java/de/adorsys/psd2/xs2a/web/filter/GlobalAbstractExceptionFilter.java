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
