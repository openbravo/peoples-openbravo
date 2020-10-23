package org.openbravo.client.kernel;

import java.util.List;

import org.hibernate.QueryException;
import org.hibernate.dialect.function.SQLFunction;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.type.BigDecimalType;
import org.hibernate.type.Type;

public class PgFullTextRankFunction implements SQLFunction {

  @Override
  public Type getReturnType(Type arg0, Mapping arg1) throws QueryException {
    return new BigDecimalType();
  }

  @Override
  public boolean hasArguments() {
    return true;
  }

  @Override
  public boolean hasParenthesesIfNoArguments() {
    return false;
  }

  @SuppressWarnings("rawtypes")
  @Override
  public String render(Type arg0, List args, SessionFactoryImplementor factory)
      throws QueryException {
    if (args == null || args.size() < 2) {
      throw new IllegalArgumentException("The function must be passed at least 1 argument");
    }

    String fragment = null;
    String field = null;
    String ftsConfiguration = null;
    String value = null;

    if (args.size() == 3) {
      field = (String) args.get(0);
      ftsConfiguration = (String) args.get(1);
      value = (String) args.get(2);
      fragment = "ts_rank_cd(" + field + ", to_tsquery(" + ftsConfiguration + "::regconfig, "
          + value + "), 4)";
    } else {
      field = (String) args.get(0);
      value = (String) args.get(1);
      fragment = "ts_rank_cd(" + field + ", to_tsquery(" + value + "), 4)";
    }

    return fragment;
  }
}
