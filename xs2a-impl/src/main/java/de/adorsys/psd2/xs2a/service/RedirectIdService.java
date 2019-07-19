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
