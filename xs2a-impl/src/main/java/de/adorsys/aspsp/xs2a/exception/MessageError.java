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

package de.adorsys.aspsp.xs2a.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import de.adorsys.aspsp.xs2a.domain.TppMessageInformation;
import de.adorsys.aspsp.xs2a.domain.TransactionStatus;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MessageError {
    @JsonUnwrapped
    @ApiModelProperty(value = "Transaction status", example = "Rejected")
    private TransactionStatus transactionStatus;

    @ApiModelProperty(value = "Tpp messages information of the Berlin Group XS2A Interface")
    private List<TppMessageInformation> tppMessages = new ArrayList<>();

    public MessageError(TppMessageInformation tppMessage) {
        this(TransactionStatus.RJCT, tppMessage);
    }

    public MessageError(List<TppMessageInformation> tppMessages) {
        this(TransactionStatus.RJCT, tppMessages);
    }

    public MessageError(TransactionStatus status, TppMessageInformation tppMessage) {
        this(status, Collections.singletonList(tppMessage));
    }

    public MessageError(TransactionStatus status, List<TppMessageInformation> tppMessages) {
        this.transactionStatus = status;
        this.tppMessages.addAll(tppMessages);
    }

    public void addTppMessage(TppMessageInformation tppMessage) {
        this.tppMessages.add(tppMessage);
    }

    // TODO task: add logic to resolve resulting MessageError https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/211
    public TppMessageInformation getTppMessage() {
        return tppMessages.get(0);
    }

}
