package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
/**
 * Links202VOPbulk
 */

@JsonTypeName("_links_202_VOPbulk")
@SuppressWarnings("PMD")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2025-09-18T08:00:00.499860948+01:00[Africa/Douala]", comments = "Generator version: 7.15.0")
public class Links202VOPbulk {

  private Optional<HrefType> self = Optional.empty();

  private Optional<HrefType> status = Optional.empty();

  public Links202VOPbulk self(HrefType self) {
    this.self = Optional.ofNullable(self);
    return this;
  }

  /**
   * Get self
   * @return self
   */
  @Valid
  @Schema(name = "self", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("self")
  public Optional<HrefType> getSelf() {
    return self;
  }

  public void setSelf(Optional<HrefType> self) {
    this.self = self;
  }

  public Links202VOPbulk status(HrefType status) {
    this.status = Optional.ofNullable(status);
    return this;
  }

  /**
   * Get status
   * @return status
   */
  @Valid
  @Schema(name = "status", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("status")
  public Optional<HrefType> getStatus() {
    return status;
  }

  public void setStatus(Optional<HrefType> status) {
    this.status = status;
  }
    /**
    * A container for additional, undeclared properties.
    * This is a holder for any undeclared properties as specified with
    * the 'additionalProperties' keyword in the OAS document.
    */
    private Map<String, HrefType> additionalProperties;

    /**
    * Set the additional (undeclared) property with the specified name and value.
    * If the property does not already exist, create it otherwise replace it.
    */
    @JsonAnySetter
    public Links202VOPbulk putAdditionalProperty(String key, HrefType value) {
        if (this.additionalProperties == null) {
            this.additionalProperties = new HashMap<String, HrefType>();
        }
        this.additionalProperties.put(key, value);
        return this;
    }

    /**
    * Return the additional (undeclared) property.
    */
    @JsonAnyGetter
    public Map<String, HrefType> getAdditionalProperties() {
        return additionalProperties;
    }

    /**
    * Return the additional (undeclared) property with the specified name.
    */
    public HrefType getAdditionalProperty(String key) {
        if (this.additionalProperties == null) {
            return null;
        }
        return this.additionalProperties.get(key);
    }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Links202VOPbulk links202VOPbulk = (Links202VOPbulk) o;
    return Objects.equals(this.self, links202VOPbulk.self) &&
        Objects.equals(this.status, links202VOPbulk.status) &&
    Objects.equals(this.additionalProperties, links202VOPbulk.additionalProperties);
  }

  @Override
  public int hashCode() {
    return Objects.hash(self, status, additionalProperties);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Links202VOPbulk {\n");
    sb.append("    self: ").append(toIndentedString(self)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");

    sb.append("    additionalProperties: ").append(toIndentedString(additionalProperties)).append("\n");
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

