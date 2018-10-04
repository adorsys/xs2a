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

package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * Content of the body of a Update PSU Authentication Request  Password subfield is used.
 */
@ApiModel(description = "Content of the body of a Update PSU Authentication Request  Password subfield is used. ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2018-08-09T18:41:17.591+02:00[Europe/Berlin]")
public class UpdatePsuAuthentication {
    @JsonProperty("psuData")
    private PsuData psuData = null;

    public UpdatePsuAuthentication psuData(PsuData psuData) {
        this.psuData = psuData;
        return this;
    }

    /**
     * Get psuData
     *
     * @return psuData
     **/
    @ApiModelProperty(required = true)
    @NotNull
    @Valid
    public PsuData getPsuData() {
        return psuData;
    }

    public void setPsuData(PsuData psuData) {
        this.psuData = psuData;
    }


    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UpdatePsuAuthentication updatePsuAuthentication = (UpdatePsuAuthentication) o;
        return Objects.equals(this.psuData, updatePsuAuthentication.psuData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(psuData);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class UpdatePsuAuthentication {\n");

        sb.append("    psuData: ").append(toIndentedString(psuData)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}

