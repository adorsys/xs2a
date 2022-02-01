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

package de.adorsys.psd2.consent.psu.api;

import de.adorsys.psd2.consent.api.CmsConstant;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;

import java.lang.annotation.*;

@Inherited
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@ApiImplicitParams({
    @ApiImplicitParam(name = CmsConstant.HEADERS.PSU_ID, value = PsuHeadersDescription.PSU_ID, paramType = PsuHeadersDescription.PARAM_TYPE),
    @ApiImplicitParam(name = CmsConstant.HEADERS.PSU_ID_TYPE, value = PsuHeadersDescription.PSU_ID_TYPE, paramType = PsuHeadersDescription.PARAM_TYPE),
    @ApiImplicitParam(name = CmsConstant.HEADERS.PSU_CORPORATE_ID, value = PsuHeadersDescription.PSU_CORPORATE_ID, paramType = PsuHeadersDescription.PARAM_TYPE),
    @ApiImplicitParam(name = CmsConstant.HEADERS.PSU_CORPORATE_ID_TYPE, value = PsuHeadersDescription.PSU_CORPORATE_ID_TYPE, paramType = PsuHeadersDescription.PARAM_TYPE)
})
public @interface PsuHeadersDescription {
    String PARAM_TYPE = "header";

    String PSU_ID = "Client ID of the PSU in the ASPSP client interface. Might be mandated in the ASPSP's documentation. Is not contained if an OAuth2 based authentication was performed in a pre-step or an OAuth2 based SCA was performed in an preceding AIS service in the same session. ";
    String PSU_ID_TYPE = "Type of the PSU-ID, needed in scenarios where PSUs have several PSU-IDs as access possibility. ";
    String PSU_CORPORATE_ID = "Might be mandated in the ASPSP's documentation. Only used in a corporate context. ";
    String PSU_CORPORATE_ID_TYPE = "Might be mandated in the ASPSP's documentation. Only used in a corporate context. ";
}
