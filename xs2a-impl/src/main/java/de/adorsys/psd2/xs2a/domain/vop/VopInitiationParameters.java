/*
 * Copyright 2018-2025 adorsys GmbH & Co KG
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
 * contact us at sales@adorsys.com.
 */

package de.adorsys.psd2.xs2a.domain.vop;

import de.adorsys.psd2.xs2a.core.security.SecuringBodyMessage;
import de.adorsys.psd2.xs2a.core.security.SigningHttpMessage;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Abstract base class for VOP initiation parameters.
 * Contains common properties such as instance ID, party verification endpoint, accept language,
 * signing HTTP message, and securing body message information.
 * 
 * <p>This class serves as the foundation for both single and bulk VOP initiation scenarios,
 * promoting code reuse and maintaining a consistent structure across different VOP operation types.</p>
 */
@Data
@NoArgsConstructor
public abstract class VopInitiationParameters {
    
    /**
     * The instance ID
     */
    @Nullable
    private String instanceId;

    /**
     * The party verification endpoint
     */
    @Nullable
    private String partyVerificationEndpoint;

    /**
     * The accept language
     */
    private  Optional<String> acceptLanguage;

    /**
     * The signing HTTP message information
     */
    private  SigningHttpMessage signingHttpMessage;

    /**
     * The securing body message information
     */
    private  SecuringBodyMessage securingBodyMessage;

    /**
     * The bulk party verification endpoint (for bulk operations)
     */
    @Nullable
    private  String bulkPartyVerificationEndpoint;

    /**
     * Indicates if the client prefers asynchronous processing (header: Client-Asynchronous-Process-Preferred)
     */
    private  Optional<Boolean> clientAsynchronousProcessPreferred;


}
