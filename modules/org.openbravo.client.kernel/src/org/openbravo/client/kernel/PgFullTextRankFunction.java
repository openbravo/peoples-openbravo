package org.openbravo.client.kernel;

import java.util.List;

import org.hibernate.QueryException;
import org.hibernate.dialect.function.SQLFunction;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.type.BigDecimalType;
import org.hibernate.type.Type;

/**
 * Creates SQL parsing for fullTextSearchRank hbm function
 */
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

  /**
   * Function that parses the hbm function to SQL language:
   * 
   * @param args:
   *          list of arguments passed to fullTextSearchFilter hbm function
   *          <li>table
   *          <li>field: tsvector column of table
   *          <li>ftsconfiguration [optional]: language to pass to to_tsquery function
   *          <li>value: string to be searched/compared
   */
  @SuppressWarnings("rawtypes")
  @Override
  public String render(Type arg0, List args, SessionFactoryImplementor factory)
      throws QueryException {
    if (args == null || args.size() < 3) {
      throw new IllegalArgumentException("The function must be passed at least 3 arguments");
    }

    int pointPosition = (int) args.get(0).toString().indexOf(".");
    String table = (String) args.get(0).toString().substring(0, pointPosition);
    String field = (String) args.get(1);
    String ftsConfiguration;
    String value;
    String fragment;

    if (args.size() == 4) {
      ftsConfiguration = (String) args.get(2);
      value = (String) args.get(3);
      fragment = "ts_rank_cd(" + table + "." + field + ", to_tsquery(" + ftsConfiguration
          + "::regconfig, " + value + "), 4)";
    } else {
      value = (String) args.get(2);
      fragment = "ts_rank_cd(" + table + "." + field + ", to_tsquery(" + value + "), 4)";
    }

    return fragment;
  }
}
