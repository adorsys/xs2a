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
