/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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

package de.adorsys.psd2.aspsp.profile.web.config;

import springfox.documentation.service.Tag;

public class AspspProfileApiTagHolder {
    public static final Tag ASPSP_PROFILE = new Tag(AspspProfileApiTagName.ASPSP_PROFILE, "Provides access to aspsp profile");
    public static final Tag UPDATE_ASPSP_PROFILE = new Tag(AspspProfileApiTagName.UPDATE_ASPSP_PROFILE, "Provides access to update aspsp profile");

    private AspspProfileApiTagHolder() {
    }
}
