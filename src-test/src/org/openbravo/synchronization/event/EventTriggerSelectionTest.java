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
package org.openbravo.synchronization.event;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.openbravo.base.weld.test.WeldBaseTest;

/**
 * Tests the {@link EventTrigger} instance selection done by the {@link SynchronizationEvent} API
 */
public class EventTriggerSelectionTest extends WeldBaseTest {

  @Test
  public void getExpectedEventTrigger() {
    String triggerName = SynchronizationEvent.getInstance()
        .getEventTrigger("EVENT_A")
        .map(trigger -> trigger.getClass().getSuperclass().getName())
        .orElse(null);
    assertThat("Expected EventTrigger found", triggerName,
        equalTo("org.openbravo.synchronization.event.TestEventTrigger"));
  }

  @Test
  public void getEventTriggerWithMorePriority() {
    String triggerName = SynchronizationEvent.getInstance()
        .getEventTrigger("EVENT_B")
        .map(trigger -> trigger.getClass().getSuperclass().getName())
        .orElse(null);
    assertThat("Expected EventTrigger found", triggerName,
        equalTo("org.openbravo.synchronization.event.AnotherTestEventTrigger"));
  }

  @Test
  public void noEventTriggerFound() {
    String triggerName = SynchronizationEvent.getInstance()
        .getEventTrigger("EVENT_C")
        .map(trigger -> trigger.getClass().getSuperclass().getName())
        .orElse(null);
    assertThat("No EventTrigger found", triggerName, nullValue());
  }
}
