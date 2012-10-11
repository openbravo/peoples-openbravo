package org.openbravo.retail.posterminal;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.poc.EmailManager;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.common.enterprise.EmailServerConfiguration;
import org.openbravo.model.common.order.Order;
import org.openbravo.utils.FormatUtilities;

public class EmailSender implements Runnable {
  private static final Logger log4j = Logger.getLogger(EmailSender.class);
  Thread runner;
  JSONObject jsonorder;
  String orderId;

  public EmailSender() {
  }

  public EmailSender(String orderId, JSONObject jsonorder) {
    this.jsonorder = jsonorder;
    this.orderId = orderId;
    runner = new Thread(this, orderId); // Create a new thread.
    runner.start(); // Start the thread.
  }

  public void run() {
    try {
      sendDocumentEmail();
    } catch (JSONException e) {
      log4j.error("JSONException: ", e);
    } catch (IOException e) {
      log4j.error("JSONException: ", e);
    } catch (ServletException e) {
      log4j.error("ServletException: ", e);
    }
  }

  public void sendDocumentEmail() throws JSONException, IOException, ServletException {
    final String toName = jsonorder.getJSONObject("bp").getString("name");
    boolean setFailed = false;
    String toEmail = jsonorder.getJSONObject("bp").getString("email");

    String emailSubject = "[Openbravo Web POS] Sale " + jsonorder.getString("documentNo")
        + " done in " + jsonorder.getString("posTerminal$_identifier");
    String emailBody = "";
    try {
      emailBody += "Date: " + jsonorder.getString("orderDate") + " \r\n";
      emailBody += "Document Number: " + jsonorder.getString("documentNo") + " \r\n";
      emailBody = "Total Gross: " + jsonorder.getString("gross") + " \r\n";
      emailBody += "Total Net: " + jsonorder.getString("net") + " \r\n";

      emailBody += "\r\n";
      emailBody += "Lines \r\n";
      emailBody += "------\r\n";

      JSONArray lines = jsonorder.getJSONArray("lines");
      for (int i = 0; i < lines.length(); i++) {
        JSONObject jsonOrderLine = lines.getJSONObject(i);
        emailBody += jsonOrderLine.getString("qty") + " - ";
        emailBody += jsonOrderLine.getString("price") + " - ";
        emailBody += jsonOrderLine.getJSONObject("product").getString("_identifier");
        emailBody += "\r\n";
      }
    } catch (Exception e) {
      emailBody = "the email body cannot be generated";
    }

    String host = null;
    boolean auth = true;
    String username = null;
    String password = null;
    String connSecurity = null;
    int port = 25;

    List<EmailServerConfiguration> emailServersConfiguration = null;
    final String replyToName = "Openbravo";
    String replyToEmail = null;
    String senderAddress = null;
    OBContext.setAdminMode(true);
    Order order = null;
    try {

      final Client client = OBDal.getInstance().get(Client.class, jsonorder.getString("client"));

      emailServersConfiguration = client.getEmailServerConfigurationList();

      final EmailServerConfiguration mailConfig = emailServersConfiguration.get(0);

      host = mailConfig.getSmtpServer();

      if (!mailConfig.isSMTPAuthentification()) {
        auth = false;
      }

      senderAddress = mailConfig.getSmtpServerSenderAddress();
      replyToEmail = mailConfig.getSmtpServerSenderAddress();
      username = mailConfig.getSmtpServerAccount();
      password = FormatUtilities.encryptDecrypt(mailConfig.getSmtpServerPassword(), false);
      connSecurity = mailConfig.getSmtpConnectionSecurity();
      port = mailConfig.getSmtpPort().intValue();
    } finally {
      OBContext.restorePreviousMode();
    }

    final String recipientTO = toEmail;
    final String recipientCC = null;
    final String recipientBCC = null;
    final String replyTo = replyToEmail;
    final String contentType = "text/plain; charset=utf-8";

    if (log4j.isDebugEnabled()) {
      log4j.debug("From: " + senderAddress);
      log4j.debug("Recipient TO (contact email): " + recipientTO);
      log4j.debug("Recipient CC: " + recipientCC);
      log4j.debug("Recipient BCC (user email): " + recipientBCC);
      log4j.debug("Reply-to (sales rep email): " + replyTo);
    }

    if ((replyToEmail == null || replyToEmail.length() == 0)) {
      setFailed = true;
      // throw new ServletException(Utility.messageBD(this, "NoSalesRepEmail", vars.getLanguage()));
      log4j.error("Cannot send email because there is no sales representative email address!");
    }

    if ((toEmail == null || toEmail.length() == 0)) {
      // throw new ServletException(Utility.messageBD(this, "NoCustomerEmail", vars.getLanguage()));
      log4j.error("Cannot send email because there is no customer email address!");
      setFailed = true;
    }

    List<File> attachments = null;

    try {
      OBContext.setAdminMode(false);
      OBDal.getInstance().get(Order.class, this.orderId);
      int cont = 1;
      while (order == null) {
        try {
          Thread.currentThread();
          Thread.sleep(2000);
        } catch (Exception e) {
          // TODO: handle exception
        }
        log4j.info("Waiting for order... " + cont);
        order = OBDal.getInstance().get(Order.class, this.orderId);
        if (cont > 5) {
          log4j.info("email cannot be send because order " + orderId
              + " can't be retrieved from database... ");
          return;
        }
        cont += 1;
      }
      if (setFailed == false) {
        EmailManager.sendEmail(host, auth, username, password, connSecurity, port, senderAddress,
            recipientTO, recipientCC, recipientBCC, replyTo, emailSubject, emailBody, contentType,
            attachments, null, null);
        order.setObposEmailStatus("Sent");
      } else {
        order.setObposEmailStatus("Failed");
      }
    } catch (Exception exception) {
      log4j.error(exception);
      final String exceptionClass = exception.getClass().toString().replace("class ", "");
      String exceptionString = "Problems while sending the email" + exception;
      exceptionString = exceptionString.replace(exceptionClass, "");
      order.setObposEmailStatus("Failed");
      throw new ServletException(exceptionString);
    } finally {
      OBDal.getInstance().save(order);
      OBDal.getInstance().flush();
      OBDal.getInstance().commitAndClose();
      OBContext.restorePreviousMode();
      log4j.info("DONE! the order" + order.getDocumentNo() + " has been sent to "
          + jsonorder.getJSONObject("bp").getString("email"));
    }

  }
}