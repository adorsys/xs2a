package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;

import java.util.Objects;

/**
 * It is contained in addition to the data element &#x27;chosenScaMethod&#x27; if challenge data is needed for SCA. In rare cases this attribute is also used in the context of the &#x27;startAuthorisationWithPsuAuthentication&#x27; link.
 */
@ApiModel(description = "It is contained in addition to the data element 'chosenScaMethod' if challenge data is needed for SCA. In rare cases this attribute is also used in the context of the 'startAuthorisationWithPsuAuthentication' link. ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2018-08-09T18:41:17.591+02:00[Europe/Berlin]")
public class ChallengeData {

    @JsonProperty("image")
    private byte[] image = null;

    @JsonProperty("data")
    private String data = null;

    @JsonProperty("imageLink")
    private String imageLink = null;

    @JsonProperty("otpMaxLength")
    private Integer otpMaxLength = null;
    @JsonProperty("otpFormat")
    private OtpFormatEnum otpFormat = null;
    @JsonProperty("additionalInformation")
    private String additionalInformation = null;

    public ChallengeData image(byte[] image) {
        this.image = image;
        return this;
    }

    /**
     * PNG data (max. 512 kilobyte) to be displayed to the PSU, Base64 encoding, cp. [RFC4648]. This attribute is used only, when PHOTO_OTP or CHIP_OTP is the selected SCA method.
     *
     * @return image
     **/
    @ApiModelProperty(value = "PNG data (max. 512 kilobyte) to be displayed to the PSU, Base64 encoding, cp. [RFC4648]. This attribute is used only, when PHOTO_OTP or CHIP_OTP is the selected SCA method. ")

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public ChallengeData data(String data) {
        this.data = data;
        return this;
    }

    /**
     * String challenge data
     *
     * @return data
     **/
    @ApiModelProperty(value = "String challenge data")

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public ChallengeData imageLink(String imageLink) {
        this.imageLink = imageLink;
        return this;
    }

    /**
     * A link where the ASPSP will provides the challenge image for the TPP.
     *
     * @return imageLink
     **/
    @ApiModelProperty(value = "A link where the ASPSP will provides the challenge image for the TPP.")

    public String getImageLink() {
        return imageLink;
    }

    public void setImageLink(String imageLink) {
        this.imageLink = imageLink;
    }

    public ChallengeData otpMaxLength(Integer otpMaxLength) {
        this.otpMaxLength = otpMaxLength;
        return this;
    }

    /**
     * The maximal length for the OTP to be typed in by the PSU.
     *
     * @return otpMaxLength
     **/
    @ApiModelProperty(value = "The maximal length for the OTP to be typed in by the PSU.")

    public Integer getOtpMaxLength() {
        return otpMaxLength;
    }

    public void setOtpMaxLength(Integer otpMaxLength) {
        this.otpMaxLength = otpMaxLength;
    }

    public ChallengeData otpFormat(OtpFormatEnum otpFormat) {
        this.otpFormat = otpFormat;
        return this;
    }

    /**
     * The format type of the OTP to be typed in. The admitted values are \"characters\" or \"integer\".
     *
     * @return otpFormat
     **/
    @ApiModelProperty(value = "The format type of the OTP to be typed in. The admitted values are \"characters\" or \"integer\".")

    public OtpFormatEnum getOtpFormat() {
        return otpFormat;
    }

    public void setOtpFormat(OtpFormatEnum otpFormat) {
        this.otpFormat = otpFormat;
    }

    public ChallengeData additionalInformation(String additionalInformation) {
        this.additionalInformation = additionalInformation;
        return this;
    }

    /**
     * Additional explanation for the PSU to explain e.g. fallback mechanism for the chosen SCA method. The TPP is obliged to show this to the PSU.
     *
     * @return additionalInformation
     **/
    @ApiModelProperty(value = "Additional explanation for the PSU to explain e.g. fallback mechanism for the chosen SCA method. The TPP is obliged to show this to the PSU. ")

    public String getAdditionalInformation() {
        return additionalInformation;
    }

    public void setAdditionalInformation(String additionalInformation) {
        this.additionalInformation = additionalInformation;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChallengeData challengeData = (ChallengeData) o;
        return Objects.equals(this.image, challengeData.image) &&
            Objects.equals(this.data, challengeData.data) &&
            Objects.equals(this.imageLink, challengeData.imageLink) &&
            Objects.equals(this.otpMaxLength, challengeData.otpMaxLength) &&
            Objects.equals(this.otpFormat, challengeData.otpFormat) &&
            Objects.equals(this.additionalInformation, challengeData.additionalInformation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(image, data, imageLink, otpMaxLength, otpFormat, additionalInformation);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ChallengeData {\n");

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
    private String toIndentedString(java.lang.Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }

    /**
     * The format type of the OTP to be typed in. The admitted values are \"characters\" or \"integer\".
     */
    public enum OtpFormatEnum {
        CHARACTERS("characters"),

        INTEGER("integer");

        private String value;

        OtpFormatEnum(String value) {
            this.value = value;
        }

        @JsonCreator
        public static OtpFormatEnum fromValue(String text) {
            for (OtpFormatEnum b : OtpFormatEnum.values()) {
                if (String.valueOf(b.value).equals(text)) {
                    return b;
                }
            }
            return null;
        }

        @Override
        @JsonValue
        public String toString() {
            return String.valueOf(value);
        }
    }
}
