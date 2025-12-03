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

import de.adorsys.psd2.xs2a.domain.CustomContentTypeProvider;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;

import java.time.OffsetDateTime;

/**
 * Common VOP domain class containing shared fields for all VOP types
 */
@Data
public class CommonVop implements CustomContentTypeProvider {
    protected String vopId;
    protected byte[] vopData;
    protected OffsetDateTime creationTimestamp;
    private String contentType;
    protected String instanceId;

    @Override
    public MediaType getCustomContentType() {
        if (StringUtils.isBlank(contentType)) {
            return MediaType.APPLICATION_JSON;
        }
        return MediaType.parseMediaType(contentType);
    }
}
