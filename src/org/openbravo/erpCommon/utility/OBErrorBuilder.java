package org.openbravo.erpCommon.utility;

import org.openbravo.erpCommon.utility.OBError;

/**
 * Provides facility methods to build OB messages ({@link OBError}).
 * 
 * @author Valery Lezhebokov
 */
public class OBErrorBuilder {

  // TODO what to do with Menu type?
  private static enum MessageType {
    Success, Confirmation, Info, Warning, Error
  }

  /**
   * Build the message based on the provided one or creates new message if a <code>null</code> was
   * passed as a base message.
   * 
   * @param message
   *          a base massage.
   * @param type
   *          a message type.
   * @param messageText
   *          the text to put into the message.
   * @return a built message.
   */
  public static OBError buildMessage(OBError message, String type, String... messageText) {

    // XXX don't we have any AssetUtils class in OB ?
    if (type == null || type.isEmpty() || messageText.length == 0) {
      throw new IllegalArgumentException(String.format(
          "Illegal arguments provided: type [%s], textMessages [%s]", type, messageText));
    }

    if (message == null) {
      final OBError newMessage = new OBError();
      newMessage.setMessage(constructTextMessage(messageText));
      newMessage.setType(type);
      return newMessage;
    } else {
      message.setMessage(constructTextMessage(message.getMessage(),
          constructTextMessage(messageText)));
      // change message type if needed
      final MessageType curType = MessageType.valueOf(message.getType());
      final MessageType newType = MessageType.valueOf(type);
      if (curType.compareTo(newType) < 0) {
        message.setType(type);
      }
      return message;
    }
  }

  /**
   * Constructs a text of the message from the set of provided {@link String}s.
   */
  private static String constructTextMessage(String... messagesText) {
    final StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append(messagesText[0]);
    for (int i = 1; i < messagesText.length; i++) {
      stringBuilder.append("<br/>").append(messagesText[i]);
    }
    return stringBuilder.toString();
  }

}
