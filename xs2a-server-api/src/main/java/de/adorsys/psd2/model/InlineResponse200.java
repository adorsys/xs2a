package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;

import java.util.Objects;

/**
 * Equals \&quot;true\&quot; if sufficient funds are available at the time of the request, \&quot;false\&quot;
 * otherwise.
 */
@ApiModel(description = "Equals \"true\" if sufficient funds are available at the time of the request, \"false\" " +
    "otherwise. ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2018-10-11T14:55" +
    ":45.627+02:00[Europe/Berlin]")
public class InlineResponse200 {
    @JsonProperty("fundsAvailable")
    private Boolean fundsAvailable = null;

    public InlineResponse200 fundsAvailable(Boolean fundsAvailable) {
        this.fundsAvailable = fundsAvailable;
        return this;
    }

    /**
     * Get fundsAvailable
     *
     * @return fundsAvailable
     **/
    @ApiModelProperty(value = "")
    public Boolean isFundsAvailable() {
        return fundsAvailable;
    }

    public void setFundsAvailable(Boolean fundsAvailable) {
        this.fundsAvailable = fundsAvailable;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        InlineResponse200 inlineResponse200 = (InlineResponse200) o;
        return Objects.equals(this.fundsAvailable, inlineResponse200.fundsAvailable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fundsAvailable);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class InlineResponse200 {\n");
        sb.append("    fundsAvailable: ").append(toIndentedString(fundsAvailable)).append("\n");
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

