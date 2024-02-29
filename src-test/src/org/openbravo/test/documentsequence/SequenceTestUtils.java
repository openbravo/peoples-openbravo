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
 * All portions are Copyright (C) 2024 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.test.documentsequence;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.SequenceUtil.CalculationMethod;
import org.openbravo.erpCommon.utility.SequenceUtil.ControlDigit;
import org.openbravo.erpCommon.utility.SequenceUtil.SequenceNumberLength;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.utility.Sequence;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.service.db.CallStoredProcedure;
import org.openbravo.service.db.DalConnectionProvider;

class SequenceTestUtils {
  static final String QA_SPAIN_ORG_ID = "357947E87C284935AD1D783CF6F099A1";

  /*
   * Creates Document Sequence to be used
   */

  static Sequence createDocumentSequence(Organization org, String sequenceName,
      CalculationMethod calculationMethod, Sequence baseSequence, String prefix, Long startingNo,
      Long nextAssignedNumber, Long incrementBy, String suffix, ControlDigit controlDigit,
      SequenceNumberLength sequenceNoLength, Long sequenceLength, boolean saveAndflush) {
    final Sequence anyExistingSequence = OBDal.getInstance()
        .getProxy(Sequence.class, "FF8080812C2ABFC6012C2B3BE4970094");
    // Create a Sequence
    final Sequence sequence = (Sequence) DalUtil.copy(anyExistingSequence);
    sequence.setOrganization(org);
    sequence.setName(sequenceName);
    sequence.setControlDigit(controlDigit.value);
    sequence.setCalculationMethod(calculationMethod.value);
    sequence.setPrefix(prefix);
    sequence.setStartingNo(startingNo == null ? 1L : startingNo);
    sequence.setNextAssignedNumber(nextAssignedNumber == null ? 1L : nextAssignedNumber);
    sequence.setIncrementBy(incrementBy == null ? 1L : incrementBy);
    sequence.setSuffix(suffix);
    sequence.setBaseSequence(baseSequence);
    sequence.setSequenceNumberLength(sequenceNoLength.value);
    sequence.setSequenceLength(sequenceLength);
    if (saveAndflush) {
      OBDal.getInstance().save(sequence);
      OBDal.getInstance().flush(); // Required to lock sequence at db level later on
    }
    return sequence;
  }

  /**
   * create document sequence with default organization QA_SPAIN_ORG_ID
   */

  public static Sequence createDocumentSequence(String sequenceName,
      CalculationMethod calculationMethod, Sequence baseSequence, String prefix, Long startingNo,
      Long nextAssignedNumber, Long incrementBy, String suffix, ControlDigit controlDigit,
      SequenceNumberLength sequenceNumberLength, Long sequenceLength, boolean saveAndflush) {
    Organization org = OBDal.getInstance().getProxy(Organization.class, QA_SPAIN_ORG_ID);
    return createDocumentSequence(org, sequenceName, calculationMethod, baseSequence, prefix,
        startingNo, nextAssignedNumber, incrementBy, suffix, controlDigit, sequenceNumberLength,
        sequenceLength, saveAndflush);
  }

  /**
   * This method sets up a child sequence with or without base sequence as per input parameters
   *
   * @param org
   *          Organization in which sequence is defined
   * @param controlDigit
   *          Control Digit for the child sequence
   * @param calculationMethod
   *          Calculation Method for the child sequence
   * @param prefix
   *          Prefix for the child sequence
   * @param startingNo
   *          Starting Number for the child sequence
   * @param nextAssignedNumber
   *          Next Assigned Number for the child sequence
   * @param incrementBy
   *          Increment Child Sequence By
   * @param suffix
   *          Suffix to be appended for the child sequence
   * @param sequenceNoLength
   *          Sequence Number Length for the child Sequence - Variable or Fix Length
   * @param sequenceLength
   *          Sequence Length for child sequence in case of Fix Length
   * @param childSequenceHasBaseSequence
   *          flag to define a base sequence for the child sequence
   * @return Document sequence to be used as base sequence in the parent sequence for referenced
   *         inventory type
   */

  public static Sequence setUpChildSequence(Organization org, String sequenceName,
      ControlDigit controlDigit, CalculationMethod calculationMethod, String prefix,
      Long startingNo, Long nextAssignedNumber, Long incrementBy, String suffix,
      SequenceNumberLength sequenceNoLength, Long sequenceLength,
      boolean childSequenceHasBaseSequence) {

    if (childSequenceHasBaseSequence) {
      Sequence childSequence = createDocumentSequence(org, sequenceName, calculationMethod, null,
          prefix, startingNo, nextAssignedNumber, incrementBy, suffix, controlDigit,
          sequenceNoLength, sequenceLength, true);
      return createDocumentSequence(org, sequenceName, CalculationMethod.SEQUENCE, childSequence,
          null, null, null, null, null, ControlDigit.NONE, sequenceNoLength, sequenceLength, true);
    }
    return createDocumentSequence(org, sequenceName, calculationMethod, null, prefix, startingNo,
        nextAssignedNumber, incrementBy, suffix, controlDigit, sequenceNoLength, sequenceLength,
        true);
  }

  /**
   * Create Document Type using
   */

  public static DocumentType createDocumentType(String docTypeId, Sequence sequence) {
    final DocumentType anyExistingDocType = OBDal.getInstance()
        .getProxy(DocumentType.class, docTypeId);
    // Create a document Type
    final DocumentType docType = (DocumentType) DalUtil.copy(anyExistingDocType);
    docType.setName(UUID.randomUUID().toString());
    docType.setCreationDate(new Date());
    docType.setSequencedDocument(true);
    docType.setDocumentSequence(sequence);
    OBDal.getInstance().save(docType);
    OBDal.getInstance().flush();
    return docType;
  }

  /**
   * Create sequence with calculation method, prefix, sequence number length and sequence length
   * 
   * @return Sequence to be used for defined Referenced Inventory Type
   */

  public static Sequence createBaseSequence(CalculationMethod calculationMethod, String prefix,
      SequenceNumberLength sequenceNumberLength, Long sequenceLength) {
    return createDocumentSequence(UUID.randomUUID().toString(), calculationMethod, null, prefix,
        null, null, null, null, ControlDigit.NONE, sequenceNumberLength, sequenceLength, false);
  }

  /**
   * Create sequence with base sequence
   * 
   * @return Sequence to be used for defined Referenced Inventory Type
   */

  public static Sequence createParentSequence(Sequence baseSequence) {
    return createDocumentSequence(UUID.randomUUID().toString(), CalculationMethod.SEQUENCE,
        baseSequence, "06", null, null, null, null, ControlDigit.MODULE10,
        SequenceNumberLength.VARIABLE, null, false);
  }

  /**
   * Create sequence with calculation method auto numbering, Fixed or Variable sequence number
   * length, sequence length
   * 
   * @return Sequence for various sequence number length and sequence length combinations
   */
  public static Sequence createSequence(SequenceNumberLength sequenceNumberLength,
      Long sequenceLength) {
    return createDocumentSequence(UUID.randomUUID().toString(), CalculationMethod.AUTONUMERING,
        null, "0110491", null, null, null, null, ControlDigit.NONE, sequenceNumberLength,
        sequenceLength, false);
  }

  /**
   * Create sequence with calculation method, baseSequence, controlDigit, prefix and suffix
   * 
   * @return Sequence to be used for validate prefix/suffix/control digit combination
   */
  public static Sequence createSequence(CalculationMethod calculationMethod, Sequence baseSequence,
      String prefix, String suffix, ControlDigit controlDigit) {
    return createDocumentSequence(UUID.randomUUID().toString(), calculationMethod, baseSequence,
        prefix, null, null, null, suffix, controlDigit, SequenceNumberLength.VARIABLE, null, false);
  }

  /**
   * 
   * Call AD_SEQUENCE_DOCUMENTNO - get documentNo using computation of sequence and control digit in
   * PL
   *
   * @param sequenceId
   *          Document Sequence configuration used to compute document No.
   * @param updateNext
   *          flag to update current next in AD_Sequence
   * @param functionName
   *          name of function to be used for computation of sequence and control digit
   * @return computed documentNo using computation of sequence and control digit in PL
   */

  public static String getDocumentNo(String sequenceId, boolean updateNext, String functionName) {
    String value = "";
    try {
      final List<Object> parameters = new ArrayList<Object>();
      parameters.add(sequenceId);
      parameters.add(updateNext);
      value = ((String) CallStoredProcedure.getInstance().call(functionName, parameters, null));
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
    return value;
  }

  /**
   * Call AD_Sequence_DocType
   */
  public static String getDocumentNo(String windowId, String tableName, String docTypeTargetId,
      String docTypeId, boolean onlyDocType, boolean updateNext) {
    return Utility.getDocumentNo(new DalConnectionProvider(false),
        RequestContext.get().getVariablesSecureApp(), windowId, tableName, docTypeTargetId,
        docTypeId, onlyDocType, updateNext);
  }

  /**
   * Call AD_Sequence_Doc
   */
  public static String getDocumentNo(String clientId, String tableName, boolean updateNext) {
    return Utility.getDocumentNo(new DalConnectionProvider(false), clientId, tableName, updateNext);
  }
}
