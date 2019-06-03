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

package de.adorsys.psd2.xs2a.core.ais;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class AccountResponseTypeTest {

    @Test
    public void fromName() {
        assertEquals(AccountResponseType.JSON, AccountResponseType.fromValue("Json"));
        assertEquals(AccountResponseType.XML, AccountResponseType.fromValue("XmL"));
        assertEquals(AccountResponseType.TEXT, AccountResponseType.fromValue("text"));

        assertNull(AccountResponseType.fromValue("234567890"));
        assertNull(AccountResponseType.fromValue(""));
        assertNull(AccountResponseType.fromValue(null));
    }
}
