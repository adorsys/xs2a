package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Objects;

/**
 * A list of hyperlinks to be recognised by the TPP.  Type of links admitted in this response:   - \&quot;
 * download\&quot;: a link to a resource, where the transaction report might be downloaded from in   case where
 * transaction reports have a huge size.  Remark: This feature shall only be used where camt-data is requested which
 * has a huge size.
 */
@ApiModel(description = "A list of hyperlinks to be recognised by the TPP.  Type of links admitted in this response: " +
    "  - \"download\": a link to a resource, where the transaction report might be downloaded from in   case where " +
    "transaction reports have a huge size.  Remark: This feature shall only be used where camt-data is requested " +
    "which has a huge size. ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2018-10-11T14:55" +
    ":45.627+02:00[Europe/Berlin]")
public class LinksDownload extends HashMap<String, String> {
    @JsonProperty("download")
    private String download = null;

    public LinksDownload download(String download) {
        this.download = download;
        return this;
    }

    /**
     * Get download
     *
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
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LinksDownload _linksDownload = (LinksDownload) o;
        return Objects.equals(this.download, _linksDownload.download) && super.equals(o);
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
    private String toIndentedString(java.lang.Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}

