/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html 
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License. 
 * The Original Code is Openbravo ERP. 
 * The Initial Developer of the Original Code is Openbravo SLU 
 * All portions are Copyright (C) 2019 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.test.system;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.openbravo.authentication.hashing.PasswordHash;

public class PasswordHashing {

  @Test
  public void sha1IsAKnownAlgorithm() {
    assertThat(PasswordHash.getAlgorithm("whatever").getClass().getSimpleName(), is("SHA1"));
  }

  @Test
  public void sha512SaltIsAKnownAlgorithm() {
    assertThat(PasswordHash.getAlgorithm("1$salt$hash").getClass().getSimpleName(),
        is("SHA512Salt"));
  }

  @Test(expected = IllegalStateException.class)
  public void unknownAlgorithmsThrowException() {
    assertThat(PasswordHash.getAlgorithm("2$salt$hash").getClass().getSimpleName(),
        is("SHA512Salt"));
  }

  @Test
  public void oldHashesWork() {
    String sha1HashedOpenbravo = "PwOd6SgWF74HY4u51bfrUxjtB9g=";
    assertThat(PasswordHash.matches("openbravo", sha1HashedOpenbravo), is(true));
  }

  @Test
  public void newHashesWork() {
    String sha512SaltedOpenbravo = "1$anySalt$iyWvhlUpOrXFPPeRVzWXXR/B4hQ5qs8ZjCLUPoncJIKHRy5HZeXm9/r20qXg8tRgKcfC8bp/u5fPPQ9qA/hheQ==";
    assertThat(PasswordHash.matches("openbravo", sha512SaltedOpenbravo), is(true));
  }

  @Test
  public void saltPrventCollission() {
    assertThat("same password should generate different salted hashes",
        PasswordHash.generateHash("mySecret"), not(equalTo(PasswordHash.generateHash("mySecret"))));
  }

}
