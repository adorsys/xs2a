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

package de.adorsys.psd2.xs2a.web.validator;

import de.adorsys.psd2.xs2a.web.validator.body.BodyValidator;
import de.adorsys.psd2.xs2a.web.validator.header.HeaderValidator;
import de.adorsys.psd2.xs2a.web.validator.path.PathParameterValidator;
import de.adorsys.psd2.xs2a.web.validator.query.QueryParameterValidator;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is a storage for all type of format validators used in XS2A. If you want to add new type of validation - this
 * should be done just here.
 *
 * If any method validator does not use all validators provided here - you may specify any combination of them. In this
 * case all other validators would be represented by empty lists.
 */
@Builder
@Data
class ValidatorWrapper {

    @Builder.Default
    private List<? extends HeaderValidator> headerValidators = new ArrayList<>();
    @Builder.Default
    private List<? extends BodyValidator> bodyValidators = new ArrayList<>();
    @Builder.Default
    private List<? extends QueryParameterValidator> queryParameterValidators = new ArrayList<>();
    @Builder.Default
    private List<? extends PathParameterValidator> pathParameterValidators = new ArrayList<>();

}
