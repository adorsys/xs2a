/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * \"following\" or \"preceding\" supported as values.  This data attribute defines the behaviour when recurring payment dates falls on a weekend or bank holiday.  The payment is then executed either the \"preceding\" or \"following\" working day. ASPSP might reject the request due to the communicated value, if rules in Online-Banking are not supporting  this execution rule. 
 */
public enum ExecutionRule {
  
  FOLLOWING("following"),
  
  PRECEDING("preceding");

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

