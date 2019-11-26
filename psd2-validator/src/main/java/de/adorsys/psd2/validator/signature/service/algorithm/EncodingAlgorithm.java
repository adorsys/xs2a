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

package de.adorsys.psd2.validator.signature.service.algorithm;


import de.adorsys.psd2.validator.signature.service.algorithm.encoding.Base64EncodingService;
import de.adorsys.psd2.validator.signature.service.algorithm.encoding.EncodingService;

public enum EncodingAlgorithm {
    BASE64("BASE64", new Base64EncodingService());

    private String algorithmName;
    private EncodingService encodingService;

    EncodingAlgorithm(String algorithmName, EncodingService encodingService) {
        this.algorithmName = algorithmName;
        this.encodingService = encodingService;
    }

    public String getAlgorithmName() {
        return algorithmName;
    }

    public EncodingService getEncodingService() {
        return encodingService;
    }

}
