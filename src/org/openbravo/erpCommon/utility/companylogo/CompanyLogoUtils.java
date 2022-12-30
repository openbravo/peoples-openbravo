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
 * All portions are Copyright (C) 2022 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.utility.companylogo;

import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.system.ClientInformation;
import org.openbravo.model.ad.system.SystemInformation;
import org.openbravo.model.ad.utility.Image;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.enterprise.OrganizationInformation;

/**
 * Utility functions used to retrieve the proper company logo image All of them looks first for the
 * given Organization, if no matching image was found, we try looking at ClientInfo and finally at
 * SystemInfo
 */
public class CompanyLogoUtils {
  public static Image getCompanyLogo(Organization org) {
    return getCompanyLogoImage(org, false);
  }

  public static Image getCompanyLogoSubmark(Organization org) {
    return getCompanyLogoSubmarkImage(org, false);
  }

  public static Image getCompanyLogoForDocuments(Organization org) {
    Image img = null;
    // first look for the logo in org
    if (org != null) {
      OrganizationInformation orgInfo = org.getOrganizationInformationList().get(0);
      img = orgInfo.getCompanyLogoForDocs();
    }
    // then try in Client info
    if (img == null) {
      ClientInformation clientInfo = OBDal.getReadOnlyInstance()
          .get(ClientInformation.class, OBContext.getOBContext().getCurrentClient().getId());
      img = clientInfo.getCompanyLogoForDocs();
    }
    // Finally try the system info
    if (img == null) {
      SystemInformation systemInfo = OBDal.getReadOnlyInstance().get(SystemInformation.class, "0");
      img = systemInfo.getCompanyLogoForDocs();
    }
    // If everything fails, return an empty image
    return img;
  }

  public static Image getCompanyLogoForReceipts(Organization org) {
    Image img = null;
    // first look for the logo in org
    if (org != null) {
      OrganizationInformation orgInfo = org.getOrganizationInformationList().get(0);
      img = orgInfo.getCompanyLogoForReceipts();
    }
    // then try in Client info
    if (img == null) {
      ClientInformation clientInfo = OBDal.getReadOnlyInstance()
          .get(ClientInformation.class, OBContext.getOBContext().getCurrentClient().getId());
      img = clientInfo.getCompanyLogoForReceipts();
    }
    // Finally try the system info
    if (img == null) {
      SystemInformation systemInfo = OBDal.getReadOnlyInstance().get(SystemInformation.class, "0");
      img = systemInfo.getCompanyLogoForReceipts();
    }
    // If everything fails, return an empty image
    return img;
  }

  private static Image getCompanyLogoSubmarkImage(Organization org, Boolean isDarkMode) {
    Image img = null;
    // first look for the logo in org
    if (org != null) {
      OrganizationInformation orgInfo = org.getOrganizationInformationList().get(0);

      if (isDarkMode) {
        img = orgInfo.getCompanyLogoSubmarkDark();
        if (img == null) {
          img = orgInfo.getCompanyLogoSubmark();
        }
      } else {
        img = orgInfo.getCompanyLogoSubmark();
        if (img == null) {
          img = orgInfo.getCompanyLogoSubmarkDark();
        }
      }
    }
    // then try in Client info
    if (img == null) {
      ClientInformation clientInfo = OBDal.getReadOnlyInstance()
          .get(ClientInformation.class, OBContext.getOBContext().getCurrentClient().getId());

      if (isDarkMode) {
        img = clientInfo.getCompanyLogoSubmarkDark();
        if (img == null) {
          img = clientInfo.getCompanyLogoSubmark();
        }
      } else {
        img = clientInfo.getCompanyLogoSubmark();
        if (img == null) {
          img = clientInfo.getCompanyLogoSubmarkDark();
        }
      }
    }
    // Finally try the system info
    if (img == null) {
      SystemInformation systemInfo = OBDal.getReadOnlyInstance().get(SystemInformation.class, "0");

      if (isDarkMode) {
        img = systemInfo.getCompanyLogoSubmarkDark();
        if (img == null) {
          img = systemInfo.getCompanyLogoSubmark();
        }
      } else {
        img = systemInfo.getCompanyLogoSubmark();
        if (img == null) {
          img = systemInfo.getCompanyLogoSubmarkDark();
        }
      }
    }
    // If everything fails, return an empty image
    return img;
  }

  private static Image getCompanyLogoImage(Organization org, Boolean isDarkMode) {
    Image img = null;
    // first look for the logo in org
    if (org != null) {
      OrganizationInformation orgInfo = org.getOrganizationInformationList().get(0);

      if (isDarkMode) {
        img = orgInfo.getCompanyLogoDark();
        if (img == null) {
          img = orgInfo.getCompanyLogo();
        }
      } else {
        img = orgInfo.getCompanyLogo();
        if (img == null) {
          img = orgInfo.getCompanyLogoDark();
        }
      }
    }
    // then try in Client info
    if (img == null) {
      ClientInformation clientInfo = OBDal.getReadOnlyInstance()
          .get(ClientInformation.class, OBContext.getOBContext().getCurrentClient().getId());

      if (isDarkMode) {
        img = clientInfo.getCompanyLogoDark();
        if (img == null) {
          img = clientInfo.getCompanyLogo();
        }
      } else {
        img = clientInfo.getCompanyLogo();
        if (img == null) {
          img = clientInfo.getCompanyLogoDark();
        }
      }
    }
    // Finally try the system info
    if (img == null) {
      SystemInformation systemInfo = OBDal.getReadOnlyInstance().get(SystemInformation.class, "0");

      if (isDarkMode) {
        img = systemInfo.getCompanyLogoDark();
        if (img == null) {
          img = systemInfo.getCompanyLogo();
        }
      } else {
        img = systemInfo.getCompanyLogo();
        if (img == null) {
          img = systemInfo.getCompanyLogoDark();
        }
      }
    }
    // If everything fails, return an empty image
    return img;
  }
}
