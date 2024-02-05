/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.0  (the  "License"),  being   the  Mozilla   Public  License
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
 *************************************************************************
 */
package org.openbravo.event;

import javax.enterprise.event.Observes;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.client.kernel.event.EntityNewEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.client.kernel.event.EntityUpdateEvent;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.materialmgmt.refinventory.ReferencedInventoryUtil;
import org.openbravo.model.ad.utility.Sequence;

/**
 * This class validates that sequence being set as base sequence should not have alphanumeric
 * prefix/suffix values if it is configured in parent sequence having control digit as Module 10.
 * 
 * Alternatively sequence that is already used as base sequence should not have alphanumeric prefix
 * and suffix values when the control digit is calculated using Module 10 algorithm in its parent
 * sequence.
 * 
 * Validates that sequences with control digit Module 10 should not have alphanumeric prefix/suffix.
 * 
 */

class ADSequenceEventHandler extends EntityPersistenceEventObserver {
  private static Entity[] entities = {
      ModelProvider.getInstance().getEntity(Sequence.ENTITY_NAME) };

  @Override
  protected Entity[] getObservedEntities() {
    return entities;
  }

  public void onSave(@Observes EntityNewEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    Sequence sequence = (Sequence) event.getTargetInstance();
    // validate sequence
    validateSequencePrefixSuffix(sequence);

    // validate base sequence
    Sequence baseSequence = sequence.getBaseSequence();
    if (baseSequence != null) {
      validateBaseSequence(baseSequence, hasControldigit(sequence));
    }
  }

  public void onUpdate(@Observes EntityUpdateEvent event) {
    if (!isValidEvent(event)) {
      return;
    }

    Sequence sequence = (Sequence) event.getTargetInstance();
    final Entity sequenceEntity = ModelProvider.getInstance().getEntity(Sequence.ENTITY_NAME);
    final Property prefixProperty = sequenceEntity.getProperty(Sequence.PROPERTY_PREFIX);
    final Property suffixProperty = sequenceEntity.getProperty(Sequence.PROPERTY_SUFFIX);
    final Property baseSequenceProperty = sequenceEntity
        .getProperty(Sequence.PROPERTY_BASESEQUENCE);
    final Property controlDigitProperty = sequenceEntity
        .getProperty(Sequence.PROPERTY_CONTROLDIGIT);

    // validate sequence

    String currentControlDigit = (String) event.getCurrentState(controlDigitProperty);
    String previousControlDigit = (String) event.getPreviousState(controlDigitProperty);
    String currentPrefix = (String) event.getCurrentState(prefixProperty);
    String previousPrefix = (String) event.getPreviousState(prefixProperty);
    String currentSuffix = (String) event.getCurrentState(suffixProperty);
    String previousSuffix = (String) event.getPreviousState(suffixProperty);
    boolean isCurrentPrefixChanged = currentPrefix != null
        && (previousPrefix == null || !previousPrefix.equals(currentPrefix));
    boolean isCurrentSuffixChanged = currentSuffix != null
        && (previousSuffix == null || !previousSuffix.equals(currentSuffix));
    boolean isControlDigitChanged = !(StringUtils.equals(previousControlDigit,
        currentControlDigit));
    if ((isCurrentPrefixChanged || isCurrentSuffixChanged || isControlDigitChanged)) {
      validateSequencePrefixSuffix(sequence);
    }

    // validate base sequence
    Sequence currentBaseSequence = (Sequence) event.getCurrentState(baseSequenceProperty);
    Sequence previousBaseSequence = (Sequence) event.getPreviousState(baseSequenceProperty);
    boolean isCurrentBaseSequenceChanged = currentBaseSequence != null
        && (previousBaseSequence == null || !previousBaseSequence.equals(currentBaseSequence));
    // When base sequence or control digit is being changed
    if (currentBaseSequence != null && (isCurrentBaseSequenceChanged || isControlDigitChanged)) {
      validateBaseSequence(currentBaseSequence, hasControldigit(sequence));
    }
  }

  /**
   * Checks whether sequence is configured with Module 10 control digit.
   * 
   * @param sequence
   *          Input Sequence
   * @return whether sequence has Module 10 control digit
   */

  private boolean hasControldigit(Sequence sequence) {
    return StringUtils.equals(sequence.getControlDigit(),
        ReferencedInventoryUtil.MODULE10_CONTROLDIGIT);
  }

  /**
   * This method validates whether base sequence has alphanumeric prefix/suffix that is being set in
   * the parent sequence with control digit Module 10. Such base sequences are not allowed
   * 
   * @param baseSequence
   *          Input sequence that is set as Base Sequence in parent Sequence
   * @param parentWithControlDigit
   *          is parent Sequence of base Sequence set with Module 10 control digit
   */

  private void validateBaseSequence(Sequence baseSequence, boolean parentWithControlDigit) {
    if (isPrefixOrSuffixAlphanumericForSequence(baseSequence) && parentWithControlDigit) {
      throw new OBException(OBMessageUtils.messageBD("ValidateBaseSequence"));
    }
  }

  /**
   * This method validates if sequence has control digit as Module 10, should not have alphanumeric
   * prefix/suffix.
   * 
   * @param sequence
   *          Input Sequence to validate
   */

  private void validateSequencePrefixSuffix(Sequence sequence) {
    if (isPrefixOrSuffixAlphanumericForSequence(sequence)) {
      if (hasControldigit(sequence)) {
        throw new OBException(OBMessageUtils.messageBD("ValidateSequence"));
      }
      if (isSeqUsedAsBaseSeqInParentSeqWithModule10ControlDigit(sequence.getId())) {
        throw new OBException(OBMessageUtils.messageBD("ValidateBaseSequence"));
      }
    }
  }

  /**
   * This method detects whether sequence has alphanumeric prefix or suffix
   * 
   * @param sequence
   *          Input sequence
   * @return whether prefix/suffix of sequence is alphanumeric
   */

  private boolean isPrefixOrSuffixAlphanumericForSequence(Sequence sequence) {
    String prefix = sequence.getPrefix();
    String suffix = sequence.getSuffix();
    return ((prefix != null && ReferencedInventoryUtil.isAlphaNumeric(prefix))
        || (suffix != null && ReferencedInventoryUtil.isAlphaNumeric(suffix)));
  }

  /**
   * This method checks that whether sequence is set as base sequence in other parent sequence whose
   * control digit is set as Module 10 to compute control digit in this case base sequence should
   * not have alphanumeric prefix/suffix.
   * 
   * @param sequenceId
   *          Input Sequence ID
   * @return Whether sequence is used as base sequence in Sequence with Module 10 control digit
   */
  private boolean isSeqUsedAsBaseSeqInParentSeqWithModule10ControlDigit(String sequenceId) {
    OBCriteria<Sequence> seqCriteria = OBDal.getInstance().createCriteria(Sequence.class);
    seqCriteria.add(Restrictions.eq(Sequence.PROPERTY_BASESEQUENCE + ".id", sequenceId));
    seqCriteria.add(Restrictions.ne(Sequence.PROPERTY_ID, sequenceId));
    seqCriteria.add(Restrictions.eq(Sequence.PROPERTY_CONTROLDIGIT,
        ReferencedInventoryUtil.MODULE10_CONTROLDIGIT));
    seqCriteria.setMaxResults(1);
    return seqCriteria.uniqueResult() != null;
  }
}
