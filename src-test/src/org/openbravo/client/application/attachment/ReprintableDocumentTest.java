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
 * All portions are Copyright (C) 2023 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.client.application.attachment;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Test;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.exception.OBSecurityException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.weld.test.WeldBaseTest;
import org.openbravo.client.application.attachment.AttachmentUtils.AttachmentType;
import org.openbravo.client.application.attachment.ReprintableDocumentManager.Format;
import org.openbravo.client.application.attachment.SourceDocument.DocumentType;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.module.Module;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.ad.utility.AttachmentConfig;
import org.openbravo.model.ad.utility.AttachmentMethod;
import org.openbravo.model.ad.utility.ReprintableDocument;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.common.order.Order;
import org.openbravo.test.base.TestConstants;

/**
 * Test cases to cover the reprintable document management infrastructure
 */
public class ReprintableDocumentTest extends WeldBaseTest {

  private static final String DATA = "<output>hello<output/>";
  private static final String INVALID_CONFIG_MSG = "The attachment type is not supported by the selected attachment method";

  @Inject
  private ReprintableDocumentManager reprintableDocumentManager;

  @After
  public void cleanUp() {
    rollback();
  }

  @Test
  public void uploadAndDownloadReprintableDocumentOfOrder() throws IOException {
    createAttachmentConfiguration(TestConstants.Clients.FB_GRP,
        createAttachmentMethod(TestAttachImplementation.SEARCH_KEY, true));

    InputStream inputStream = new ByteArrayInputStream(DATA.getBytes(StandardCharsets.UTF_8));
    SourceDocument sourceDocument = new SourceDocument(TEST_ORDER_ID, DocumentType.ORDER);
    Order order = OBDal.getInstance().get(Order.class, TEST_ORDER_ID);

    ReprintableDocument document = reprintableDocumentManager.upload(inputStream, Format.XML,
        sourceDocument);
    assertThat(document.getFormat(), equalTo(Format.XML.name()));
    assertThat(document.getOrder().getId(), equalTo(order.getId()));
    assertThat(document.getClient().getId(), equalTo(order.getClient().getId()));
    assertThat(document.getOrganization().getId(), equalTo(order.getOrganization().getId()));
    assertThat(document.getName(), equalTo("reprintableDocument.xml"));

    OBDal.getInstance().flush();

    InputStream downloadedStream = reprintableDocumentManager.download(sourceDocument);
    String result = new String(downloadedStream.readAllBytes(), StandardCharsets.UTF_8);
    assertThat(result, equalTo(DATA));
  }

  @Test
  public void uploadAndDownloadReprintableDocumentOfInvoice() throws IOException {
    createAttachmentConfiguration(TestConstants.Clients.FB_GRP,
        createAttachmentMethod(TestAttachImplementation.SEARCH_KEY, true));

    InputStream inputStream = new ByteArrayInputStream(DATA.getBytes(StandardCharsets.UTF_8));
    SourceDocument sourceDocument = new SourceDocument(TEST_INVOICE_ID, DocumentType.INVOICE);
    Invoice invoice = OBDal.getInstance().get(Invoice.class, TEST_INVOICE_ID);

    ReprintableDocument document = reprintableDocumentManager.upload(inputStream, Format.XML,
        sourceDocument);
    assertThat(document.getFormat(), equalTo(Format.XML.name()));
    assertThat(document.getInvoice().getId(), equalTo(invoice.getId()));
    assertThat(document.getClient().getId(), equalTo(invoice.getClient().getId()));
    assertThat(document.getOrganization().getId(), equalTo(invoice.getOrganization().getId()));
    assertThat(document.getName(), equalTo("reprintableDocument.xml"));

    OBDal.getInstance().flush();

    InputStream downloadedStream = reprintableDocumentManager.download(sourceDocument);
    String result = new String(downloadedStream.readAllBytes(), StandardCharsets.UTF_8);
    assertThat(result, equalTo(DATA));
  }

  @Test
  public void cannotUploadReprintableDocumentOfNonWritableDocument() {
    createAttachmentConfiguration(TestConstants.Clients.SYSTEM,
        createAttachmentMethod(TestAttachImplementation.SEARCH_KEY, true));

    setQAAdminContext();

    InputStream inputStream = new ByteArrayInputStream(DATA.getBytes(StandardCharsets.UTF_8));
    SourceDocument sourceDocument = new SourceDocument(TEST_ORDER_ID, DocumentType.ORDER);

    assertThrows(OBSecurityException.class,
        () -> reprintableDocumentManager.upload(inputStream, Format.XML, sourceDocument));
  }

  @Test
  public void cannotDownloadReprintableDocumentOfNonWritableDocument() {
    createAttachmentConfiguration(TestConstants.Clients.FB_GRP,
        createAttachmentMethod(TestAttachImplementation.SEARCH_KEY, true));

    InputStream inputStream = new ByteArrayInputStream(DATA.getBytes(StandardCharsets.UTF_8));
    SourceDocument sourceDocument = new SourceDocument(TEST_ORDER_ID, DocumentType.ORDER);
    reprintableDocumentManager.upload(inputStream, Format.XML, sourceDocument);
    OBDal.getInstance().flush();

    setQAAdminContext();

    assertThrows(OBSecurityException.class,
        () -> reprintableDocumentManager.download(sourceDocument));
  }

  @Test
  public void cannotSaveInvalidAttachmentConfiguration() {
    AttachmentMethod attachmentMethod = createAttachmentMethod(TestAttachImplementation.SEARCH_KEY,
        false);

    OBException thrown = assertThrows(OBException.class,
        () -> createAttachmentConfiguration(TestConstants.Clients.FB_GRP, attachmentMethod));
    assertThat(thrown.getMessage(), equalTo(INVALID_CONFIG_MSG));
  }

  @Test
  public void cannotUpdateInvalidAttachmentConfiguration() {
    AttachmentMethod reprintableDocAttachmentMethod = createAttachmentMethod(
        TestAttachImplementation.SEARCH_KEY, true);
    AttachmentMethod standardAttachmentMethod = createAttachmentMethod("ANOTHER_METHOD", false);
    AttachmentConfig config = createAttachmentConfiguration(TestConstants.Clients.FB_GRP,
        reprintableDocAttachmentMethod);

    OBException thrown = assertThrows(OBException.class, () -> {
      config.setAttachmentMethod(standardAttachmentMethod);
      OBDal.getInstance().flush();
    });
    assertThat(thrown.getMessage(), equalTo(INVALID_CONFIG_MSG));
  }

  private AttachmentConfig createAttachmentConfiguration(String clientId,
      AttachmentMethod attachmentMethod) {
    try {
      OBContext.setAdminMode(false);
      AttachmentConfig config = OBProvider.getInstance().get(AttachmentConfig.class);
      config.setClient(OBDal.getInstance().getProxy(Client.class, clientId));
      config.setOrganization(
          OBDal.getInstance().getProxy(Organization.class, TestConstants.Orgs.MAIN));
      config.setAttachmentType(AttachmentType.RD.name());
      config.setAttachmentMethod(attachmentMethod);

      OBDal.getInstance().save(config);
      OBDal.getInstance().flush();

      return config;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  private AttachmentMethod createAttachmentMethod(String searchKey,
      boolean supportReprintableDocuments) {
    try {
      OBContext.setAdminMode(false);
      setCoreInDevelopment(true);
      AttachmentMethod attachmentMethod = OBProvider.getInstance().get(AttachmentMethod.class);
      attachmentMethod
          .setClient(OBDal.getInstance().getProxy(Client.class, TestConstants.Clients.SYSTEM));
      attachmentMethod.setOrganization(
          OBDal.getInstance().getProxy(Organization.class, TestConstants.Orgs.MAIN));
      attachmentMethod
          .setModule(OBDal.getInstance().getProxy(Module.class, TestConstants.Modules.ID_CORE));
      attachmentMethod.setName(searchKey);
      attachmentMethod.setValue(searchKey);
      attachmentMethod.setSupportReprintableDocuments(supportReprintableDocuments);
      OBDal.getInstance().save(attachmentMethod);
      OBDal.getInstance().flush();
      setCoreInDevelopment(false);
      return attachmentMethod;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  private void setCoreInDevelopment(boolean inDevelopment) {
    try {
      OBContext.setAdminMode(false);
      OBDal.getInstance()
          .get(Module.class, TestConstants.Modules.ID_CORE)
          .setInDevelopment(inDevelopment);
      OBDal.getInstance().flush();
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}
