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

package de.adorsys.psd2.xs2a.service;

import de.adorsys.psd2.xs2a.domain.RedirectIdHolder;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedirectIdService {
    private final RedirectIdHolder redirectIdHolder;

    /**
     * Generates redirect ID based on authorisation ID
     *
     * @param authorisationId authorisation ID
     * @return redirect ID
     */
    public String generateRedirectId(String authorisationId) {
        redirectIdHolder.setRedirectId(authorisationId);
        return authorisationId;
    }

    /**
     * Returns redirect ID, used in the current request.
     * <p>
     * May not exist if the ID was not generated yet or if the current flow doesn't require redirect ID to be generated.
     *
     * @return redirect ID, if it was already generated, <code>null</code> otherwise
     */
    @Nullable
    public String getRedirectId() {
        return redirectIdHolder.getRedirectId();
    }
}
