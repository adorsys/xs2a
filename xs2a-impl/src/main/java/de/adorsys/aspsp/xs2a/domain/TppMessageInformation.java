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

package de.adorsys.aspsp.xs2a.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import de.adorsys.aspsp.xs2a.exception.MessageCategory;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Size;

@Data
@ApiModel(description = "Tpp Message Information", value = "TppMessageInformation")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TppMessageInformation {

	@ApiModelProperty(value = "Category of the error, Only ”ERROR” or \"WARNING\" permitted", required = true, example = "Error")
	private MessageCategory category;

	@ApiModelProperty(value = "Code", required = true)
	private MessageCode code;

    @ApiModelProperty(value = "Path")
    private String path;

	@ApiModelProperty(value = "Additional explanation text", example = "Additional text information of the ASPSP up to 512 characters")
    @Size(max = 512)
	private String text;

    public TppMessageInformation(MessageCategory category, MessageCode code) {
        this.category = category;
        this.code = code;
    }

    public TppMessageInformation path(String path){
        this.path = path;
        return this;
    }

    public TppMessageInformation text(String text){
        this.text = text;
        return this;
    }
}
