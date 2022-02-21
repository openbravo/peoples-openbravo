package org.openbravo.service.importqueue;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.service.importprocess.ImportEntry;
import org.openbravo.service.importprocess.ImportEntryPreProcessor;
import org.openbravo.service.importqueue.impl.NoQueue;

@ApplicationScoped
public class QueueManager implements ImportEntryProcessor {

  @Inject
  @Any
  private Instance<RecordProcessor> queueProcessorsList;

  private QueueImplementation queue;

  @PostConstruct
  private void init() {
    queue = new NoQueue();
    queue.start(this);
  }

  /**
   * Creates and saves the import entry, calls the
   * {@link ImportEntryPreProcessor#beforeCreate(ImportEntry)} on the
   * {@link ImportEntryPreProcessor} instances.
   * 
   * Note will commit the session/connection using {@link OBDal#commitAndClose()}
   */
  public void publishImportEntry(String qualifier, String json) {
    queue.publish(qualifier, json);
  }

  @Override
  public void processImportEntry(String qualifier, String json) throws QueueException {

    OBContext cnx = OBContext.getOBContext();
    String originalUser = cnx.getUser().getId();
    String originalRole = cnx.getRole().getId();
    String originalClient = cnx.getCurrentClient().getId();
    String originalOrg = cnx.getCurrentOrganization().getId();

    RecordProcessor processor = queueProcessorsList.select(new QueueManager.Selector(qualifier))
        .get();

    OBContext.setAdminMode(false);
    try {
      JSONObject jsonObject = new JSONObject(json);
      JSONArray data = jsonObject.getJSONArray("data");
      for (int i = 0; i < data.length(); i++) {
        try {
          JSONObject record = data.getJSONObject(i);
          final String orgId = getOrganizationId(record, originalOrg);
          final String userId = getUserId(record, originalUser);
          OBContext.setOBContext(userId, originalRole, originalClient, orgId);
          processor.processRecord(record);
        } catch (Exception e) {
          throw new QueueException(e.getMessage(), e);
        }
      }
    } catch (JSONException e) {
      throw new QueueException(e.getMessage(), e);
    } finally {
      OBContext.setOBContext(originalUser, originalRole, originalClient, originalOrg);
      OBContext.restorePreviousMode();
    }
  }

  private String getOrganizationId(final JSONObject jsonRecord, final String currentOrg)
      throws JSONException {
    if (jsonRecord.has("trxOrganization")) {
      return jsonRecord.getString("trxOrganization");
    }
    if (jsonRecord.has("organization")) {
      return jsonRecord.getString("organization");
    }
    return currentOrg;
  }

  private String getUserId(final JSONObject jsonRecord, final String currentUserId)
      throws JSONException {
    if (jsonRecord.has("updatedBy") && !"null".equals(jsonRecord.getString("updatedBy"))) {
      return jsonRecord.getString("updatedBy");
    }
    if (jsonRecord.has("createdBy") && !"null".equals(jsonRecord.getString("createdBy"))) {
      return jsonRecord.getString("createdBy");
    }
    if (jsonRecord.has("userId") && !"null".equals(jsonRecord.getString("userId"))) {
      return jsonRecord.getString("userId");
    }
    return currentUserId;
  }

  /**
   * Defines the qualifier used to register a component provider.
   */
  @javax.inject.Qualifier
  @Retention(RetentionPolicy.RUNTIME)
  @Target({ ElementType.FIELD, ElementType.METHOD, ElementType.TYPE })
  public @interface Qualifier {
    String value();
  }

  /**
   * A class used to select the correct component provider.
   * 
   */
  @SuppressWarnings("all")
  public static class Selector extends AnnotationLiteral<QueueManager.Qualifier>
      implements QueueManager.Qualifier {
    private static final long serialVersionUID = 1L;

    private final String value;

    public Selector(String value) {
      this.value = value;
    }

    @Override
    public String value() {
      return value;
    }
  }
}
