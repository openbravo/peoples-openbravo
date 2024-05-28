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
import static org.openbravo.materialmgmt.refinventory.ReferencedInventoryTestUtils.createHandlingUnit;
import static org.openbravo.materialmgmt.refinventory.ReferencedInventoryTestUtils.createHandlingUnitType;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.verification.VerificationMode;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.weld.test.WeldBaseTest;
import org.openbravo.dal.service.OBDal;
import org.openbravo.materialmgmt.refinventory.ReferencedInventoryStatusProcessor.ReferencedInventoryStatus;
import org.openbravo.model.materialmgmt.onhandquantity.ReferencedInventory;
import org.openbravo.synchronization.event.SynchronizationEvent;

/**
 * Test cases to cover the handling unit status changing using the
 * {@link ReferencedInventoryStatusProcessor} class
 */
public class ReferencedInventoryStatusProcessorTest extends WeldBaseTest {

  private ReferencedInventory container;
  private ReferencedInventory pallet;
  private ReferencedInventory box;

  @Inject
  private ReferencedInventoryStatusProcessor statusProcessor;

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
    assertThat(container.getStatus(), equalTo(ReferencedInventoryStatus.OPEN.name()));
    assertThat(pallet.getStatus(), equalTo(ReferencedInventoryStatus.OPEN.name()));
    assertThat(box.getStatus(), equalTo(ReferencedInventoryStatus.OPEN.name()));
  }

  @Test
  public void changeStatusInCascade() {
    ReferencedInventoryStatus newStatus = ReferencedInventoryStatus.CLOSED;
    statusProcessor.changeHandlingUnitStatus(container, newStatus);
    assertThat(container.getStatus(), equalTo(newStatus.name()));
    assertThat(pallet.getStatus(), equalTo(newStatus.name()));
    assertThat(box.getStatus(), equalTo(newStatus.name()));

    newStatus = ReferencedInventoryStatus.OPEN;
    statusProcessor.changeHandlingUnitStatus(container, newStatus);
    assertThat(container.getStatus(), equalTo(newStatus.name()));
    assertThat(pallet.getStatus(), equalTo(newStatus.name()));
    assertThat(box.getStatus(), equalTo(newStatus.name()));

    newStatus = ReferencedInventoryStatus.DESTROYED;
    statusProcessor.changeHandlingUnitStatus(container, newStatus);
    assertThat(container.getStatus(), equalTo(newStatus.name()));
    assertThat(pallet.getStatus(), equalTo(newStatus.name()));
    assertThat(box.getStatus(), equalTo(newStatus.name()));
  }

  @Test
  public void changeIntermediateHandlingUnitStatus() {
    statusProcessor.changeHandlingUnitStatus(pallet, ReferencedInventoryStatus.CLOSED);
    assertThat(container.getStatus(), equalTo(ReferencedInventoryStatus.OPEN.name()));
    assertThat(pallet.getStatus(), equalTo(ReferencedInventoryStatus.CLOSED.name()));
    assertThat(box.getStatus(), equalTo(ReferencedInventoryStatus.CLOSED.name()));
  }

  @Test
  public void changeSingleHandlingUnitStatus() {
    statusProcessor.changeHandlingUnitStatus(box, ReferencedInventoryStatus.CLOSED);
    assertThat(container.getStatus(), equalTo(ReferencedInventoryStatus.OPEN.name()));
    assertThat(pallet.getStatus(), equalTo(ReferencedInventoryStatus.OPEN.name()));
    assertThat(box.getStatus(), equalTo(ReferencedInventoryStatus.CLOSED.name()));
  }

  @Test
  public void changeStatusAtDifferentLevels() {
    statusProcessor.changeHandlingUnitStatus(container, ReferencedInventoryStatus.CLOSED);
    statusProcessor.changeHandlingUnitStatus(container, ReferencedInventoryStatus.OPEN);
    statusProcessor.changeHandlingUnitStatus(box, ReferencedInventoryStatus.DESTROYED);
    statusProcessor.changeHandlingUnitStatus(pallet, ReferencedInventoryStatus.CLOSED);

    assertThat(container.getStatus(), equalTo(ReferencedInventoryStatus.OPEN.name()));
    assertThat(pallet.getStatus(), equalTo(ReferencedInventoryStatus.CLOSED.name()));
    assertThat(box.getStatus(), equalTo(ReferencedInventoryStatus.DESTROYED.name()));
  }

  @Test
  public void cannotChangeStatusOfADestroyedHandlingUnit() {
    statusProcessor.changeHandlingUnitStatus(box, ReferencedInventoryStatus.DESTROYED);
    OBException exception = assertThrows(OBException.class,
        () -> statusProcessor.changeHandlingUnitStatus(box, ReferencedInventoryStatus.OPEN));
    assertThat(exception.getMessage(),
        equalTo("Cannot change the status of a destroyed handling unit"));
  }

  @Test
  public void cannotOpenAHandlingUnitWithParentClosed() {
    statusProcessor.changeHandlingUnitStatus(pallet, ReferencedInventoryStatus.CLOSED);

    OBException exception = assertThrows(OBException.class,
        () -> statusProcessor.changeHandlingUnitStatus(box, ReferencedInventoryStatus.OPEN));
    assertThat(exception.getMessage(), equalTo(
        "Cannot change the status of the handling unit B1 because its parent handling unit P1 is closed"));
  }

  @Test
  public void cannotDestroyAHandlingUnitWithParentClosed() {
    statusProcessor.changeHandlingUnitStatus(pallet, ReferencedInventoryStatus.CLOSED);

    OBException exception = assertThrows(OBException.class,
        () -> statusProcessor.changeHandlingUnitStatus(box, ReferencedInventoryStatus.DESTROYED));
    assertThat(exception.getMessage(), equalTo(
        "Cannot change the status of the handling unit B1 because its parent handling unit P1 is closed"));
  }

  @Test
  public void cannotOpenAHandlingUnitWithAllParentsClosed() {
    statusProcessor.changeHandlingUnitStatus(container, ReferencedInventoryStatus.CLOSED);

    OBException exception = assertThrows(OBException.class,
        () -> statusProcessor.changeHandlingUnitStatus(box, ReferencedInventoryStatus.OPEN));
    assertThat(exception.getMessage(), equalTo(
        "Cannot change the status of the handling unit B1 because its parent handling unit P1 is closed"));
  }

  @Test
  public void cannotDestroyAHandlingUnitWithAllParentsClosed() {
    statusProcessor.changeHandlingUnitStatus(container, ReferencedInventoryStatus.CLOSED);

    OBException exception = assertThrows(OBException.class,
        () -> statusProcessor.changeHandlingUnitStatus(box, ReferencedInventoryStatus.DESTROYED));
    assertThat(exception.getMessage(), equalTo(
        "Cannot change the status of the handling unit B1 because its parent handling unit P1 is closed"));
  }

  @Test
  public void eventIsTriggeredWhenExpected() {
    changeStatusAndVerifyTriggeredEvent(container, ReferencedInventoryStatus.CLOSED, times(1));
    changeStatusAndVerifyTriggeredEvent(container, ReferencedInventoryStatus.CLOSED, never());
    changeStatusAndVerifyTriggeredEvent(container, ReferencedInventoryStatus.OPEN, times(1));
    changeStatusAndVerifyTriggeredEvent(container, ReferencedInventoryStatus.OPEN, never());
    changeStatusAndVerifyTriggeredEvent(pallet, ReferencedInventoryStatus.OPEN, never());
    changeStatusAndVerifyTriggeredEvent(box, ReferencedInventoryStatus.OPEN, never());
    changeStatusAndVerifyTriggeredEvent(box, ReferencedInventoryStatus.DESTROYED, times(1));
    changeStatusAndVerifyTriggeredEvent(pallet, ReferencedInventoryStatus.CLOSED, times(1));
    changeStatusAndVerifyTriggeredEvent(container, ReferencedInventoryStatus.CLOSED, times(1));
  }

  private void changeStatusAndVerifyTriggeredEvent(ReferencedInventory handlingUnit,
      ReferencedInventoryStatus status, VerificationMode mode) {
    mockStatic(SynchronizationEvent.class, synchronizationEventMock -> {
      SynchronizationEvent instanceMock = mock(SynchronizationEvent.class);
      synchronizationEventMock.when(SynchronizationEvent::getInstance).thenReturn(instanceMock);
      statusProcessor.changeHandlingUnitStatus(handlingUnit, status);
      verify(instanceMock, mode).triggerEvent("API_HandlingUnitStatusChange", handlingUnit.getId());
    });
  }
}
