package org.openbravo.advpaymentmngt.hqlinjections;

import javax.enterprise.context.ApplicationScoped;

import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.type.StandardBasicTypes;
import org.openbravo.client.kernel.ApplicationInitializer;
import org.openbravo.dal.service.OBDal;

@ApplicationScoped
public class AprmApplicationInitializer implements ApplicationInitializer {

  public void initialize() {
    OBDal.getInstance().registerSQLFunction("array_to_string",
        new StandardSQLFunction("array_to_string", StandardBasicTypes.STRING));
    OBDal.getInstance().registerSQLFunction("array_agg",
        new StandardSQLFunction("array_agg", StandardBasicTypes.STRING));
    OBDal.getInstance().registerSQLFunction("get_uuid",
        new StandardSQLFunction("get_uuid", StandardBasicTypes.STRING));
  }
}