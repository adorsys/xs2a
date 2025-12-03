package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * See document \\\&quot;openFinance API Framework Data Dictionary\\\&quot;, section \\\&quot;Challenge\\\&quot; for more details.
 */

@Schema(name = "challenge", description = "See document \\\"openFinance API Framework Data Dictionary\\\", section \\\"Challenge\\\" for more details. ")
@JsonTypeName("challenge")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2025-09-18T08:00:00.499860948+01:00[Africa/Douala]", comments = "Generator version: 7.15.0")
public class Challenge {

  private Optional<String> image = Optional.empty();

  @Valid
  private List<String> data = new ArrayList<>();

  private Optional<String> imageLink = Optional.empty();

  private Optional<Integer> otpMaxLength = Optional.empty();

  private Optional<String> otpFormat = Optional.empty();

  private Optional<String> additionalInformation = Optional.empty();

  public Challenge image(String image) {
    this.image = Optional.ofNullable(image);
    return this;
  }

  /**
   * PNG data (max. 512 kilobyte) to be displayed to the PSU, Base64 encoding, cp. [RFC4648]. This attribute is used only, when PHOTO_OTP or CHIP_OTP is the selected SCA method.
   * @return image
   */

  @Schema(name = "image", description = "PNG data (max. 512 kilobyte) to be displayed to the PSU, Base64 encoding, cp. [RFC4648]. This attribute is used only, when PHOTO_OTP or CHIP_OTP is the selected SCA method. ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("image")
  public Optional<String> getImage() {
    return image;
  }

  public void setImage(Optional<String> image) {
    this.image = image;
  }

  public Challenge data(List<String> data) {
    this.data = data;
    return this;
  }

  public Challenge addDataItem(String dataItem) {
    if (this.data == null) {
      this.data = new ArrayList<>();
    }
    this.data.add(dataItem);
    return this;
  }

  /**
   * A collection of challenge data
   * @return data
   */

  @Schema(name = "data", description = "A collection of challenge data ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("data")
  public List<String> getData() {
    return data;
  }

  public void setData(List<String> data) {
    this.data = data;
  }

  public Challenge imageLink(String imageLink) {
    this.imageLink = Optional.ofNullable(imageLink);
    return this;
  }

  /**
   * A link where the ASPSP will provides the challenge image for the TPP.
   * @return imageLink
   */

  @Schema(name = "imageLink", description = "A link where the ASPSP will provides the challenge image for the TPP. ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("imageLink")
  public Optional<String> getImageLink() {
    return imageLink;
  }

  public void setImageLink(Optional<String> imageLink) {
    this.imageLink = imageLink;
  }

  public Challenge otpMaxLength(Integer otpMaxLength) {
    this.otpMaxLength = Optional.ofNullable(otpMaxLength);
    return this;
  }

  /**
   * The maximal length for the OTP to be typed in by the PSU.
   * @return otpMaxLength
   */

  @Schema(name = "otpMaxLength", description = "The maximal length for the OTP to be typed in by the PSU. ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("otpMaxLength")
  public Optional<Integer> getOtpMaxLength() {
    return otpMaxLength;
  }

  public void setOtpMaxLength(Optional<Integer> otpMaxLength) {
    this.otpMaxLength = otpMaxLength;
  }

  public Challenge otpFormat(String otpFormat) {
    this.otpFormat = Optional.ofNullable(otpFormat);
    return this;
  }

  /**
   * The format type of the OTP to be typed in. The admitted values are \\\"characters\\\" or \\\"integer\\\".
   * @return otpFormat
   */

  @Schema(name = "otpFormat", description = "The format type of the OTP to be typed in. The admitted values are \\\"characters\\\" or \\\"integer\\\". ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("otpFormat")
  public Optional<String> getOtpFormat() {
    return otpFormat;
  }

  public void setOtpFormat(Optional<String> otpFormat) {
    this.otpFormat = otpFormat;
  }

  public Challenge additionalInformation(String additionalInformation) {
    this.additionalInformation = Optional.ofNullable(additionalInformation);
    return this;
  }

  /**
   * Additional explanation for the PSU to explain e.g. fallback mechanism for the chosen SCA method. The TPP is obliged to show this to the PSU.
   * @return additionalInformation
   */

  @Schema(name = "additionalInformation", description = "Additional explanation for the PSU to explain e.g. fallback mechanism for the chosen SCA method. The TPP is obliged to show this to the PSU. ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("additionalInformation")
  public Optional<String> getAdditionalInformation() {
    return additionalInformation;
  }

  public void setAdditionalInformation(Optional<String> additionalInformation) {
    this.additionalInformation = additionalInformation;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Challenge challenge = (Challenge) o;
    return Objects.equals(this.image, challenge.image) &&
        Objects.equals(this.data, challenge.data) &&
        Objects.equals(this.imageLink, challenge.imageLink) &&
        Objects.equals(this.otpMaxLength, challenge.otpMaxLength) &&
        Objects.equals(this.otpFormat, challenge.otpFormat) &&
        Objects.equals(this.additionalInformation, challenge.additionalInformation);
  }

  @Override
  public int hashCode() {
    return Objects.hash(image, data, imageLink, otpMaxLength, otpFormat, additionalInformation);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Challenge {\n");
    sb.append("    image: ").append(toIndentedString(image)).append("\n");
    sb.append("    data: ").append(toIndentedString(data)).append("\n");
    sb.append("    imageLink: ").append(toIndentedString(imageLink)).append("\n");
    sb.append("    otpMaxLength: ").append(toIndentedString(otpMaxLength)).append("\n");
    sb.append("    otpFormat: ").append(toIndentedString(otpFormat)).append("\n");
    sb.append("    additionalInformation: ").append(toIndentedString(additionalInformation)).append("\n");
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

