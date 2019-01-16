package de.adorsys.psd2.model;

import java.util.Objects;
import io.swagger.annotations.ApiModel;
import com.fasterxml.jackson.annotation.JsonValue;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * \"following\" or \"preceeding\" supported as values.  This data attribute defines the behavior when recurring payment dates falls on a weekend or bank holiday.  The payment is then executed either the \"preceeding\" or \"following\" working day. ASPSP might reject the request due to the communicated value, if rules in Online-Banking are not supporting  this execution rule. 
 */
public enum ExecutionRule {
  
  FOLLOWING("following"),
  
  PRECEEDING("preceeding");

  private String value;

  ExecutionRule(String value) {
    this.value = value;
  }

  @Override
  @JsonValue
  public String toString() {
    return String.valueOf(value);
  }

  @JsonCreator
  public static ExecutionRule fromValue(String text) {
    for (ExecutionRule b : ExecutionRule.values()) {
      if (String.valueOf(b.value).equals(text)) {
        return b;
      }
    }
    return null;
  }
}

