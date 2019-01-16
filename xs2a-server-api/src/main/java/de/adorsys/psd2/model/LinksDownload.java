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

package de.adorsys.psd2.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.HashMap;
import java.util.Map;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * A list of hyperlinks to be recognised by the TPP.  Type of links admitted in this response:   - \&quot;download\&quot;: a link to a resource, where the transaction report might be downloaded from in    case where transaction reports have a huge size.  Remark: This feature shall only be used where camt-data is requested which has a huge size. 
 */
@ApiModel(description = "A list of hyperlinks to be recognised by the TPP.  Type of links admitted in this response:   - \"download\": a link to a resource, where the transaction report might be downloaded from in    case where transaction reports have a huge size.  Remark: This feature shall only be used where camt-data is requested which has a huge size. ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-01-11T12:48:04.675377+02:00[Europe/Kiev]")

public class LinksDownload extends HashMap<String, String>  {
  @JsonProperty("download")
  private String download = null;

  public LinksDownload download(String download) {
    this.download = download;
    return this;
  }

  /**
   * Get download
   * @return download
  **/
  @ApiModelProperty(required = true, value = "")
  @NotNull


  public String getDownload() {
    return download;
  }

  public void setDownload(String download) {
    this.download = download;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LinksDownload _linksDownload = (LinksDownload) o;
    return Objects.equals(this.download, _linksDownload.download) &&
        super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(download, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class LinksDownload {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    download: ").append(toIndentedString(download)).append("\n");
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

