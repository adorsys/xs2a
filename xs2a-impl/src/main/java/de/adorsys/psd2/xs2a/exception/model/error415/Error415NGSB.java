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

package de.adorsys.psd2.xs2a.exception.model.error415;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

// TODO remove, when specification provide a class for such error code https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/634

/**
 * NextGenPSD2 specific definition of reporting error information in case of a HTTP error code 415.
 */
@ApiModel(description = "NextGenPSD2 specific definition of reporting error information in case of a HTTP error code 415. ")
@Validated
public class Error415NGSB {
    @JsonProperty("tppMessages")
    @Valid
    private List<TppMessage415SB> tppMessages = null;

    @JsonProperty("_links")
    private Map _links = null;

    public Error415NGSB tppMessages(List<TppMessage415SB> tppMessages) {
        this.tppMessages = tppMessages;
        return this;
    }

    public Error415NGSB addTppMessagesItem(TppMessage415SB tppMessagesItem) {
        if (this.tppMessages == null) {
            this.tppMessages = new ArrayList<>();
        }
        this.tppMessages.add(tppMessagesItem);
        return this;
    }

    /**
     * Get tppMessages
     *
     * @return tppMessages
     **/
    @ApiModelProperty(value = "")
    @Valid
    public List<TppMessage415SB> getTppMessages() {
        return tppMessages;
    }

    public void setTppMessages(List<TppMessage415SB> tppMessages) {
        this.tppMessages = tppMessages;
    }

    public Error415NGSB _links(Map _links) {
        this._links = _links;
        return this;
    }

    /**
     * Get _links
     *
     * @return _links
     **/
    @ApiModelProperty(value = "")
    @Valid
    @JsonProperty("_links")
    public Map getLinks() {
        return _links;
    }

    public void setLinks(Map _links) {
        this._links = _links;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Error415NGSB error415NGSB = (Error415NGSB) o;
        return Objects.equals(this.tppMessages, error415NGSB.tppMessages) &&
                   Objects.equals(this._links, error415NGSB._links);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tppMessages, _links);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Error415NGSB {\n");

        sb.append("    tppMessages: ").append(toIndentedString(tppMessages)).append("\n");
        sb.append("    _links: ").append(toIndentedString(_links)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}

