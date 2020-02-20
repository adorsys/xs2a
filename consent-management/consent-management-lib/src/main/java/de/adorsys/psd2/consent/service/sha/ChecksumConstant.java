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

package de.adorsys.psd2.consent.service.sha;

public class ChecksumConstant {
    public static final String DELIMITER = "_%_";
    public static final int VERSION_START_POSITION = 0;
    public static final int CONSENT_CHECKSUM_START_POSITION = 1;
    public static final int ASPSP_ACCESS_CHECKSUM_START_POSITION = 2;

    private ChecksumConstant() {
    }
}
