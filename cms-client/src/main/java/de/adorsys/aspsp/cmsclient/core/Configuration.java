/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
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

package de.adorsys.aspsp.cmsclient.core;

import de.adorsys.aspsp.cmsclient.cms.CmsServiceInvoker;

public class Configuration {
    private String cmsServicesBaseUrl;
    private int connectTimeout;
    private int connectionRequestTimeout;

    public Configuration(String cmsServicesBaseUrl, int connectTimeout, int connectionRequestTimeout) {
        this.cmsServicesBaseUrl = cmsServicesBaseUrl;
        this.connectTimeout = connectTimeout;
        this.connectionRequestTimeout = connectionRequestTimeout;
    }

    public CmsServiceInvoker getRestServiceInvoker() {
        final RestClientConfig restClientConfig = new RestClientConfig();
        restClientConfig.setBaseServiceUrl(cmsServicesBaseUrl);
        restClientConfig.setConnectTimeout(connectTimeout);
        restClientConfig.setConnectionRequestTimeout(connectionRequestTimeout);
        return new CmsServiceInvoker(restClientConfig);
    }
}
