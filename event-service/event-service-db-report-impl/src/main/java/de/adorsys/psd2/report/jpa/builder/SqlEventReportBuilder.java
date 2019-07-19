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

package de.adorsys.psd2.report.jpa.builder;

import org.apache.commons.io.IOUtils;

import java.io.IOException;


public interface SqlEventReportBuilder {

    SqlEventReportBuilder instanceId();

    SqlEventReportBuilder period();

    SqlEventReportBuilder consentId();

    SqlEventReportBuilder paymentId();

    SqlEventReportBuilder eventType();

    SqlEventReportBuilder eventOrigin();

    String build();

    SqlEventReportBuilder baseRequest();

    default String getBasePartOfRequest(String fileName) throws IOException {
        return IOUtils.toString(getClass().getClassLoader().getResource(fileName).openStream());
    }
}
