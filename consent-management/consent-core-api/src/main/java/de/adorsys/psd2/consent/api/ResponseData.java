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

package de.adorsys.psd2.consent.api;

import lombok.Data;

@Data
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
