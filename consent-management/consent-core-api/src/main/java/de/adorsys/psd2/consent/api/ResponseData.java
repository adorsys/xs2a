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

package de.adorsys.psd2.consent.api;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ResponseData<D> {

    private D data;
    private CmsPageInfo pageInfo;
    private Object status;

    public ResponseData(D data, Object status) {
        this.data = data;
        this.status = status;
    }

    public ResponseData(D data, CmsPageInfo pageInfo, Object status) {
        this.data = data;
        this.pageInfo = pageInfo;
        this.status = status;
    }

    public static <D> ResponseData<D> entity(D data, Object status) {
        return new ResponseData<>(data, status);
    }

    public static <D> ResponseData<D> list(D data, CmsPageInfo info, Object status) {
        return new ResponseData<>(data, info, status);
    }
}
