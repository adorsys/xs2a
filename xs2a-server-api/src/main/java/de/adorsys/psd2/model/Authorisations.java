package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * An array of all authorisationIds.
 */
@ApiModel(description = "An array of all authorisationIds.")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2021-11-05T12:22:49.487689+02:00[Europe/Kiev]")

public class Authorisations   {
  @JsonProperty("authorisationIds")
  private AuthorisationsList authorisationIds = null;

  public Authorisations authorisationIds(AuthorisationsList authorisationIds) {
    this.authorisationIds = authorisationIds;
    return this;
  }

  /**
   * Get authorisationIds
   * @return authorisationIds
  **/
  @ApiModelProperty(required = true, value = "")
  @NotNull

  @Valid


  @JsonProperty("authorisationIds")
  public AuthorisationsList getAuthorisationIds() {
    return authorisationIds;
  }

  public void setAuthorisationIds(AuthorisationsList authorisationIds) {
    this.authorisationIds = authorisationIds;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
}    Authorisations authorisations = (Authorisations) o;
    return Objects.equals(this.authorisationIds, authorisations.authorisationIds);
  }

  @Override
  public int hashCode() {
    return Objects.hash(authorisationIds);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Authorisations {\n");

    sb.append("    authorisationIds: ").append(toIndentedString(authorisationIds)).append("\n");
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

