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
        PasswordHash.getDefaultAlgorithm().generateHash("mySecret"),
        not(equalTo(PasswordHash.getDefaultAlgorithm().generateHash("mySecret"))));
  }

}
