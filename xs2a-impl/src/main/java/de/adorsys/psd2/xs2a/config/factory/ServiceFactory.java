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

package de.adorsys.psd2.xs2a.config.factory;

public interface ServiceFactory {
    /**
     * Returns a particular service depending on service name provided as a method argument
     *
     * @param serviceName String representation of requested service name which should match the value from @Service("serviceName") annotation
     * @return T particular service
     */
    <T> T getService(String serviceName);
}
