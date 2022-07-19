package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Objects;

/**
 * TppMessage405PIS
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2022-05-06T13:00:42.214155+03:00[Europe/Kiev]")


public class TppMessage405PIS   {
  @JsonProperty("category")
  private TppMessageCategory category = null;

  @JsonProperty("code")
  private String code = null;

  @JsonProperty("path")
  private String path = null;

  @JsonProperty("text")
  private String text = null;

  public TppMessage405PIS category(TppMessageCategory category) {
    this.category = category;
    return this;
  }

  /**
   * Get category
   * @return category
   **/
  @Schema(required = true, description = "")
      @NotNull

    @Valid
    public TppMessageCategory getCategory() {
    return category;
  }

  public void setCategory(TppMessageCategory category) {
    this.category = category;
  }

  public TppMessage405PIS code(String code) {
    this.code = code;
    return this;
  }

  /**
   * Message codes defined for payment cancelations PIS for HTTP Error code 405 (METHOD NOT ALLOWED).
   * @return code
   **/
  @Schema(required = true, description = "Message codes defined for payment cancelations PIS for HTTP Error code 405 (METHOD NOT ALLOWED).")
      @NotNull

    public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public TppMessage405PIS path(String path) {
    this.path = path;
    return this;
  }

  /**
   * Get path
   * @return path
   **/
  @Schema(description = "")

    public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public TppMessage405PIS text(String text) {
    this.text = text;
    return this;
  }

  /**
   * Additional explaining text to the TPP.
   * @return text
   **/
  @Schema(description = "Additional explaining text to the TPP.")

  @Size(max=500)   public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TppMessage405PIS tppMessage405PIS = (TppMessage405PIS) o;
    return Objects.equals(this.category, tppMessage405PIS.category) &&
        Objects.equals(this.code, tppMessage405PIS.code) &&
        Objects.equals(this.path, tppMessage405PIS.path) &&
        Objects.equals(this.text, tppMessage405PIS.text);
  }

  @Override
  public int hashCode() {
    return Objects.hash(category, code, path, text);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TppMessage405PIS {\n");

    sb.append("    category: ").append(toIndentedString(category)).append("\n");
    sb.append("    code: ").append(toIndentedString(code)).append("\n");
    sb.append("    path: ").append(toIndentedString(path)).append("\n");
    sb.append("    text: ").append(toIndentedString(text)).append("\n");
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
