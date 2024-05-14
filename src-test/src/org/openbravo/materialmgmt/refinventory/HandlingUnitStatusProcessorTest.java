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
package org.openbravo.materialmgmt.refinventory;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.openbravo.materialmgmt.refinventory.HandlingUnitTestUtils.createHandlingUnit;
import static org.openbravo.materialmgmt.refinventory.HandlingUnitTestUtils.createHandlingUnitType;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.verification.VerificationMode;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.weld.test.WeldBaseTest;
import org.openbravo.dal.service.OBDal;
import org.openbravo.materialmgmt.refinventory.HandlingUnitStatusProcessor.HandlingUnitStatus;
import org.openbravo.model.materialmgmt.onhandquantity.ReferencedInventory;
import org.openbravo.synchronization.event.SynchronizationEvent;

/**
 * Test cases to cover the handling unit status changing using the
 * {@link HandlingUnitStatusProcessor} class
 */
public class HandlingUnitStatusProcessorTest extends WeldBaseTest {

  private ReferencedInventory container;
  private ReferencedInventory pallet;
  private ReferencedInventory box;

  @Inject
  private HandlingUnitStatusProcessor statusProcessor;

  @Before
  public void prepareHandlingUnits() {
    container = createHandlingUnit("C1", createHandlingUnitType("Container"));
    pallet = createHandlingUnit("P1", createHandlingUnitType("Pallet"));
    pallet.setParentRefInventory(container);
    box = createHandlingUnit("B1", createHandlingUnitType("Box"));
    box.setParentRefInventory(pallet);
    OBDal.getInstance().flush();
  }

  @After
  public void cleanUp() {
    rollback();
  }

  @Test
  public void handlingUnitsAreOpenedByDefault() {
    assertThat(container.getStatus(), equalTo(HandlingUnitStatus.OPEN.name()));
    assertThat(pallet.getStatus(), equalTo(HandlingUnitStatus.OPEN.name()));
    assertThat(box.getStatus(), equalTo(HandlingUnitStatus.OPEN.name()));
  }

  @Test
  public void changeStatusInCascade() {
    HandlingUnitStatus newStatus = HandlingUnitStatus.CLOSED;
    statusProcessor.changeHandlingUnitStatus(container, newStatus);
    assertThat(container.getStatus(), equalTo(newStatus.name()));
    assertThat(pallet.getStatus(), equalTo(newStatus.name()));
    assertThat(box.getStatus(), equalTo(newStatus.name()));

    newStatus = HandlingUnitStatus.OPEN;
    statusProcessor.changeHandlingUnitStatus(container, newStatus);
    assertThat(container.getStatus(), equalTo(newStatus.name()));
    assertThat(pallet.getStatus(), equalTo(newStatus.name()));
    assertThat(box.getStatus(), equalTo(newStatus.name()));

    newStatus = HandlingUnitStatus.DESTROYED;
    statusProcessor.changeHandlingUnitStatus(container, newStatus);
    assertThat(container.getStatus(), equalTo(newStatus.name()));
    assertThat(pallet.getStatus(), equalTo(newStatus.name()));
    assertThat(box.getStatus(), equalTo(newStatus.name()));
  }

  @Test
  public void changeIntermediateHandlingUnitStatus() {
    statusProcessor.changeHandlingUnitStatus(pallet, HandlingUnitStatus.CLOSED);
    assertThat(container.getStatus(), equalTo(HandlingUnitStatus.OPEN.name()));
    assertThat(pallet.getStatus(), equalTo(HandlingUnitStatus.CLOSED.name()));
    assertThat(box.getStatus(), equalTo(HandlingUnitStatus.CLOSED.name()));
  }

  @Test
  public void changeSingleHandlingUnitStatus() {
    statusProcessor.changeHandlingUnitStatus(box, HandlingUnitStatus.CLOSED);
    assertThat(container.getStatus(), equalTo(HandlingUnitStatus.OPEN.name()));
    assertThat(pallet.getStatus(), equalTo(HandlingUnitStatus.OPEN.name()));
    assertThat(box.getStatus(), equalTo(HandlingUnitStatus.CLOSED.name()));
  }

  @Test
  public void changeStatusAtDifferentLevels() {
    statusProcessor.changeHandlingUnitStatus(container, HandlingUnitStatus.CLOSED);
    statusProcessor.changeHandlingUnitStatus(container, HandlingUnitStatus.OPEN);
    statusProcessor.changeHandlingUnitStatus(box, HandlingUnitStatus.DESTROYED);
    statusProcessor.changeHandlingUnitStatus(pallet, HandlingUnitStatus.CLOSED);

    assertThat(container.getStatus(), equalTo(HandlingUnitStatus.OPEN.name()));
    assertThat(pallet.getStatus(), equalTo(HandlingUnitStatus.CLOSED.name()));
    assertThat(box.getStatus(), equalTo(HandlingUnitStatus.DESTROYED.name()));
  }

  @Test
  public void cannotChangeStatusOfADestroyedHandlingUnit() {
    statusProcessor.changeHandlingUnitStatus(box, HandlingUnitStatus.DESTROYED);
    OBException exception = assertThrows(OBException.class,
        () -> statusProcessor.changeHandlingUnitStatus(box, HandlingUnitStatus.OPEN));
    assertThat(exception.getMessage(),
        equalTo("Cannot change the status of a destroyed handling unit"));
  }

  @Test
  public void cannotOpenAHandlingUnitWithParentClosed() {
    statusProcessor.changeHandlingUnitStatus(pallet, HandlingUnitStatus.CLOSED);

    OBException exception = assertThrows(OBException.class,
        () -> statusProcessor.changeHandlingUnitStatus(box, HandlingUnitStatus.OPEN));
    assertThat(exception.getMessage(), equalTo(
        "Cannot change the status of the handling unit B1 because its parent handling unit P1 is closed"));
  }

  @Test
  public void cannotDestroyAHandlingUnitWithParentClosed() {
    statusProcessor.changeHandlingUnitStatus(pallet, HandlingUnitStatus.CLOSED);

    OBException exception = assertThrows(OBException.class,
        () -> statusProcessor.changeHandlingUnitStatus(box, HandlingUnitStatus.DESTROYED));
    assertThat(exception.getMessage(), equalTo(
        "Cannot change the status of the handling unit B1 because its parent handling unit P1 is closed"));
  }

  @Test
  public void cannotOpenAHandlingUnitWithAllParentsClosed() {
    statusProcessor.changeHandlingUnitStatus(container, HandlingUnitStatus.CLOSED);

    OBException exception = assertThrows(OBException.class,
        () -> statusProcessor.changeHandlingUnitStatus(box, HandlingUnitStatus.OPEN));
    assertThat(exception.getMessage(), equalTo(
        "Cannot change the status of the handling unit B1 because its parent handling unit P1 is closed"));
  }

  @Test
  public void cannotDestroyAHandlingUnitWithAllParentsClosed() {
    statusProcessor.changeHandlingUnitStatus(container, HandlingUnitStatus.CLOSED);

    OBException exception = assertThrows(OBException.class,
        () -> statusProcessor.changeHandlingUnitStatus(box, HandlingUnitStatus.DESTROYED));
    assertThat(exception.getMessage(), equalTo(
        "Cannot change the status of the handling unit B1 because its parent handling unit P1 is closed"));
  }

  @Test
  public void eventIsTriggeredWhenExpected() {
    changeStatusAndVerifyTriggeredEvent(container, HandlingUnitStatus.CLOSED, times(1));
    changeStatusAndVerifyTriggeredEvent(container, HandlingUnitStatus.CLOSED, never());
    changeStatusAndVerifyTriggeredEvent(container, HandlingUnitStatus.OPEN, times(1));
    changeStatusAndVerifyTriggeredEvent(container, HandlingUnitStatus.OPEN, never());
    changeStatusAndVerifyTriggeredEvent(pallet, HandlingUnitStatus.OPEN, never());
    changeStatusAndVerifyTriggeredEvent(box, HandlingUnitStatus.OPEN, never());
    changeStatusAndVerifyTriggeredEvent(box, HandlingUnitStatus.DESTROYED, times(1));
    changeStatusAndVerifyTriggeredEvent(pallet, HandlingUnitStatus.CLOSED, times(1));
    changeStatusAndVerifyTriggeredEvent(container, HandlingUnitStatus.CLOSED, times(1));
  }

  private void changeStatusAndVerifyTriggeredEvent(ReferencedInventory handlingUnit,
      HandlingUnitStatus status, VerificationMode mode) {
    mockStatic(SynchronizationEvent.class, synchronizationEventMock -> {
      SynchronizationEvent instanceMock = mock(SynchronizationEvent.class);
      synchronizationEventMock.when(SynchronizationEvent::getInstance).thenReturn(instanceMock);
      statusProcessor.changeHandlingUnitStatus(handlingUnit, status);
      verify(instanceMock, mode).triggerEvent("API_HandlingUnitStatusChange", handlingUnit.getId());
    });
  }
}
