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
 * All portions are Copyright (C) 2018 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.test.system;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import org.junit.Test;
import org.openbravo.dal.core.OBContext;
import org.openbravo.test.base.OBBaseTest;

/** Tests covering {@link OBContext}. */
public class OBContextTest extends OBBaseTest {

  @Test
  public void basicSerializationShouldWork() throws IOException, ClassNotFoundException {
    OBContext originalCtx = OBContext.getOBContext();
    Path serializedPath = serializeContext(originalCtx);
    try {
      OBContext deserialized = deserializeContext(serializedPath);
      System.out.println(deserialized.getRole());
      assertThat("Role ID is kept", deserialized.getRole().getId(), is(originalCtx.getRole()
          .getId()));
    } finally {
      Files.delete(serializedPath);
    }
  }

  @Test
  public void clientVisisilityInCorrectAfterDeserialization() throws IOException,
      ClassNotFoundException {
    OBContext originalCtx = OBContext.getOBContext();
    Path serializedPath = serializeContext(originalCtx);
    try {
      OBContext deserialized = deserializeContext(serializedPath);
      System.out.println(deserialized.getRole());

      assertThat("Readable clients are kept", Arrays.asList(originalCtx.getReadableClients()),
          containsInAnyOrder(originalCtx.getReadableClients()));
    } finally {
      Files.delete(serializedPath);
    }
  }

  @Test
  public void organizationVisisilityInCorrectAfterDeserialization() throws IOException,
      ClassNotFoundException {
    OBContext originalCtx = OBContext.getOBContext();
    Path serializedPath = serializeContext(originalCtx);
    try {
      OBContext deserialized = deserializeContext(serializedPath);
      System.out.println(deserialized.getRole());

      assertThat("Readable organizations are kept",
          Arrays.asList(originalCtx.getReadableOrganizations()),
          containsInAnyOrder(originalCtx.getReadableOrganizations()));
      assertThat("Writable organizations are kept", originalCtx.getWritableOrganizations(),
          containsInAnyOrder(originalCtx.getWritableOrganizations().toArray()));
    } finally {
      Files.delete(serializedPath);
    }
  }

  private Path serializeContext(OBContext ctx) throws IOException {
    Path serializedPath = Files.createTempFile("serialized", ".tmp");
    try (OutputStream o = Files.newOutputStream(serializedPath);
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(o)) {
      objectOutputStream.writeObject(ctx);
    }
    return serializedPath;
  }

  private OBContext deserializeContext(Path path) throws IOException, ClassNotFoundException {
    try (InputStream is = Files.newInputStream(path);
        ObjectInputStream ois = new ObjectInputStream(is)) {
      return (OBContext) ois.readObject();
    }
  }
}
