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

package de.adorsys.psd2.consent.web.psu;

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
