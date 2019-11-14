package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * An array of all cancellationIds.
 */
@ApiModel(description = "An array of all cancellationIds.")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-11-11T13:48:52.194360+02:00[Europe/Kiev]")

public class Cancellations   {
  @JsonProperty("cancellationIds")
  private CancellationList cancellationIds = null;

  public Cancellations cancellationIds(CancellationList cancellationIds) {
    this.cancellationIds = cancellationIds;
    return this;
  }

  /**
   * Get cancellationIds
   * @return cancellationIds
  **/
  @ApiModelProperty(required = true, value = "")
  @NotNull

  @Valid


  @JsonProperty("cancellationIds")
  public CancellationList getCancellationIds() {
    return cancellationIds;
  }

  public void setCancellationIds(CancellationList cancellationIds) {
    this.cancellationIds = cancellationIds;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Cancellations cancellations = (Cancellations) o;
    return Objects.equals(this.cancellationIds, cancellations.cancellationIds);
  }

  @Override
  public int hashCode() {
    return Objects.hash(cancellationIds);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Cancellations {\n");

    sb.append("    cancellationIds: ").append(toIndentedString(cancellationIds)).append("\n");
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

