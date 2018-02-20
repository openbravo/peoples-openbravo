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
 * All portions are Copyright (C) 2008-2018 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.test.security;

import static com.google.common.collect.Sets.newHashSet;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.security.OrganizationStructureProvider;
import org.openbravo.test.base.OBBaseTest;

import com.google.common.collect.ImmutableMap;

/**
 * Tests computation of natural tree of an organization. This is used to compute the readable
 * organizations of a user.
 * 
 * @see OrganizationStructureProvider
 * @see OBContext#getReadableOrganizations()
 * 
 * @author mtaal
 */

@RunWith(Parameterized.class)
public class AllowedOrganizationsTest extends OBBaseTest {
  private static final String MAIN = "0";
  private static final String FB_GROUP = "19404EAD144C49A0AF37D54377CF452D";
  private static final String US = "2E60544D37534C0B89E765FE29BC0B43";
  private static final String US_EST = "7BABA5FF80494CAFA54DEBD22EC46F01";
  private static final String US_WEST = "BAE22373FEBE4CCCA24517E23F0C8A48";
  private static final String ESP = "B843C30461EA4501935CB1D125C9C25A";
  private static final String ESP_SUR = "DC206C91AA6A4897B44DA897936E0EC3";
  private static final String ESP_NORTE = "E443A31992CB4635AFCAEABE7183CE85";

  private static final Map<String, String> ORG_NAMES = ImmutableMap.<String, String> builder()
      .put(MAIN, "Main") //
      .put(FB_GROUP, "F&B International Group") //
      .put(US, "F&B US, Inc.") //
      .put(US_EST, "F&B US East Coast") //
      .put(US_WEST, "F&B US West Coast") //
      .put(ESP, "F&B España, S.A") //
      .put(ESP_SUR, "F&B España - Región Sur") //
      .put(ESP_NORTE, "F&B España - Región Norte").build();

  private static final Map<String, Set<String>> ORG_TREES = ImmutableMap
      .<String, Set<String>> builder()
      .put(FB_GROUP, newHashSet(MAIN, FB_GROUP, US, US_EST, US_WEST, ESP, ESP_SUR, ESP_NORTE))
      .put(US, newHashSet(MAIN, FB_GROUP, US, US_EST, US_WEST))
      .put(US_WEST, newHashSet(MAIN, FB_GROUP, US, US_WEST))
      .put(US_EST, newHashSet(MAIN, FB_GROUP, US, US_EST))
      .put(ESP, newHashSet(MAIN, FB_GROUP, ESP, ESP_SUR, ESP_NORTE))
      .put(ESP_SUR, newHashSet(MAIN, FB_GROUP, ESP, ESP_SUR))
      .put(ESP_NORTE, newHashSet(MAIN, FB_GROUP, ESP, ESP_NORTE)) //
      .put("Dummy", newHashSet("Dummy")) // special case: non-existent org returns itself
      .build();

  @Parameter(0)
  public String testingOrgName;

  @Parameter(1)
  public String testingOrgId;

  @Parameter(2)
  public Set<String> expectedNaturalTree;

  @Parameters(name = "Natural tree of {0}")
  public static Collection<Object[]> parameters() throws IOException {
    final Collection<Object[]> allTrees = new ArrayList<>();

    for (Entry<String, Set<String>> tree : ORG_TREES.entrySet()) {
      allTrees.add(new Object[] { //
          ORG_NAMES.get(tree.getKey()), //
              tree.getKey(), //
              tree.getValue() //
          });
    }

    return allTrees;
  }

  /**
   * Tests valid organizations trees for different organizations.
   */
  @Test
  public void testOrganizationTree() {
    setTestAdminContext();

    final OrganizationStructureProvider osp = new OrganizationStructureProvider();
    osp.setClientId(TEST_CLIENT_ID);

    assertThat("Natural tree for " + ORG_NAMES.get(testingOrgId), osp.getNaturalTree(testingOrgId),
        is(expectedNaturalTree));
  }
}