/*
 ************************************************************************************
 * Copyright (C) 2022 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import org.junit.Before;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.weld.test.WeldBaseTest;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.security.OrganizationStructureProvider;
import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.test.base.TestConstants.Users;

public class PriceExceptionBaseTest extends WeldBaseTest {

  public static final String WHITE_VALLEY = "39363B0921BB4293B48383844325E84C";
  public static final String WHITE_VALLEY_GROUP = "67839EEFA49E44AC969BD60093FCC899";
  public static final String VALL_BLANCA = "D270A5AC50874F8BA67A88EE977F8E3B";
  public static final String WHITE_VALLEY_ADM = "E717F902C44C455793463450495FF36B";
  public static final String NORTH_EAST_ZONE = "14B1927026BE471E9B85FE699BCA61C2";
  public static final String SOUTH_WEST_ZONE = "4399136852B145BD96CC2A6CE0800C68";

  public static final String PRICE_LIST_SCHEMA = "13D3CC6DB93343DAB06E600414D35E83";
  public static final String PRICE_LIST = "496CF965DF9744D2A41248392D1DE407";

  @Before
  public void setContext() {
    OBContext.setOBContext(Users.OPENBRAVO, WHITE_VALLEY_ADM, WHITE_VALLEY, VALL_BLANCA);

  }

  protected long calculateOrgDepth(int depth, Organization org) {
    OrganizationStructureProvider osp = null;
    try {
      osp = OBContext.getOBContext().getOrganizationStructureProvider(org.getClient().getId());
    } catch (Exception e) {
      throw new OBException("Could not get org structure provider "e.getMessage());
    }
    if (org.getId().equals("0")) {
      return depth;
    } else {
      return calculateOrgDepth(depth + 1, osp.getParentOrg(org));
    }
  }

  protected Date plusDaysToCurrentDate(int days) {
    return Date.from(LocalDateTime.now().plusDays(days).atZone(ZoneId.systemDefault()).toInstant());
  }

  protected Date minusDaysFromCurrentDate(int days) {
    return Date
        .from(LocalDateTime.now().minusDays(days).atZone(ZoneId.systemDefault()).toInstant());
  }

  protected Instant hoursAgo(int hours) {
    return LocalDateTime.now().minusHours(hours).atZone(ZoneId.systemDefault()).toInstant();
  }

  protected String getUUID() {
    return SequenceIdData.getUUID();
  }

}
