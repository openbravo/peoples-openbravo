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
 * All portions are Copyright (C) 2016-2017 Openbravo SLU 
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.test.base;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;

/**
 * Used in {@link OBBaseTest}, keeps track of all messages written in log in order to make possible
 * to later do assertions on them.
 *
 * @author alostale
 *
 */
public class TestLogAppender extends AppenderSkeleton {
  private Map<Level, List<String>> messages = new HashMap<Level, List<String>>();
  private boolean logStackTraces = false;

  @Override
  protected void append(LoggingEvent event) {
    List<String> levelMsgs = messages.get(event.getLevel());
    if (levelMsgs == null) {
      levelMsgs = new ArrayList<String>();
      messages.put(event.getLevel(), levelMsgs);
    }
    levelMsgs.add(event.getMessage().toString());
    if (logStackTraces && event.getThrowableStrRep() != null) {
      levelMsgs.addAll(Arrays.asList(event.getThrowableStrRep()));
    }
  }

  @Override
  public void close() {
  }

  @Override
  public boolean requiresLayout() {
    return false;
  }

  /** Include in messages possible stack traces for logged Throwables */
  void setLogStackTraces(boolean logStackTraces) {
    this.logStackTraces = logStackTraces;
  }

  /** Removes all the messages tracked so far */
  public void reset() {
    messages = new HashMap<Level, List<String>>();
    logStackTraces = false;
  }

  /**
   * Returns a list with all messaged currently tracked. If none is tracked, an empty list is
   * returned.
   */
  public List<String> getAllMessages() {
    List<String> allMessages = new ArrayList<String>();
    for (Entry<Level, List<String>> msgLvl : messages.entrySet()) {
      allMessages.addAll(msgLvl.getValue());
    }
    return allMessages;
  }

  /** Returns a list of tracked message for a given Level, or an empty if none is tracked */
  public List<String> getMessages(Level level) {
    List<String> msgs = messages.get(level);
    if (msgs == null) {
      msgs = Collections.emptyList();
    }
    return msgs;
  }
}
