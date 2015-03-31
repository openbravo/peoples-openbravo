package org.openbravo.retail.posterminal;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.domain.Reference;

public class ErrorComparator implements Comparator<OBPOSErrors> {
  private static String TYPES_REFERENCE = "20A228A295C844C68B4451622057A893";
  private Map<String, Long> sequenceNumbers = new HashMap<String, Long>();

  public ErrorComparator() {
    super();
    OBContext.setAdminMode(false);
    try {
      Reference ref = OBDal.getInstance().get(Reference.class, TYPES_REFERENCE);
      for (org.openbravo.model.ad.domain.List listRef : ref.getADListList()) {
        if (listRef.getSequenceNumber() != null) {
          sequenceNumbers.put(listRef.getSearchKey(), listRef.getSequenceNumber());
        }
      }
    } finally {
      OBContext.restorePreviousMode();
    }

  }

  @Override
  public int compare(OBPOSErrors error1, OBPOSErrors error2) {
    Long l1 = sequenceNumbers.get(error1.getTypeofdata());
    Long l2 = sequenceNumbers.get(error2.getTypeofdata());
    if (l1 == null && l2 == null) {
      return 0;
    } else if (l1 == null && l2 != null) {
      return -1;
    } else if (l1 != null && l2 == null) {
      return 1;
    } else {
      return l1.compareTo(l2);
    }
  }

}
