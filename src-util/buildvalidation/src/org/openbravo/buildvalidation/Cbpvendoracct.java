package org.openbravo.buildvalidation;

import java.util.ArrayList;

import org.openbravo.database.ConnectionProvider;

public class Cbpvendoracct extends BuildValidation {

  public ArrayList<String> execute() {
    ConnectionProvider cp = getConnectionProvider();
    ArrayList<String> errors = new ArrayList<String>();
    try {
      int a=Integer.parseInt(CbpvendoracctData.countNoDistinct(cp));
      int b=Integer.parseInt(CbpvendoracctData.countDistinct(cp));
      if(a!=b){
          errors.add("Error message for Amedios validation (TBD)");
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return errors;
  }

}
