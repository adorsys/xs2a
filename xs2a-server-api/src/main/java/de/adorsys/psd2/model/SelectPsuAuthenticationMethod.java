package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * Content of the body of a Select PSU Authentication Method Request
 */
@ApiModel(description = "Content of the body of a Select PSU Authentication Method Request ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2018-08-09T18:41:17.591+02:00[Europe/Berlin]")
public class SelectPsuAuthenticationMethod {

    @JsonProperty("authenticationMethodId")
    private String authenticationMethodId = null;

    public SelectPsuAuthenticationMethod authenticationMethodId(String authenticationMethodId) {
        this.authenticationMethodId = authenticationMethodId;
        return this;
    }

    /**
     * Get authenticationMethodId
     *
     * @return authenticationMethodId
     **/
    @ApiModelProperty(required = true)
    @NotNull

    public String getAuthenticationMethodId() {
        return authenticationMethodId;
    }

    public void setAuthenticationMethodId(String authenticationMethodId) {
        this.authenticationMethodId = authenticationMethodId;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SelectPsuAuthenticationMethod selectPsuAuthenticationMethod = (SelectPsuAuthenticationMethod) o;
        return Objects.equals(this.authenticationMethodId, selectPsuAuthenticationMethod.authenticationMethodId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(authenticationMethodId);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class SelectPsuAuthenticationMethod {\n");

        sb.append("    authenticationMethodId: ").append(toIndentedString(authenticationMethodId)).append("\n");
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
