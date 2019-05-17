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

package de.adorsys.psd2.xs2a.core.pis;

import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.*;

public class PisExecutionRuleTest {

    @Test
    public void getByValue() {
        assertEquals(Optional.of(PisExecutionRule.FOLLOWING), PisExecutionRule.getByValue("following"));
        assertEquals(Optional.of(PisExecutionRule.PRECEDING), PisExecutionRule.getByValue("preceding"));

        assertEquals(Optional.empty(), PisExecutionRule.getByValue("98765432"));
        assertEquals(Optional.empty(), PisExecutionRule.getByValue(""));
        assertEquals(Optional.empty(), PisExecutionRule.getByValue(null));
    }
}
