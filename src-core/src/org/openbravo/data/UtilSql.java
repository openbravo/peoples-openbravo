/*
 ************************************************************************************
 * Copyright (C) 2001-2006 Openbravo S.L.
 * Licensed under the Apache Software License version 2.0
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to  in writing,  software  distributed
 * under the License is distributed  on  an  "AS IS"  BASIS,  WITHOUT  WARRANTIES  OR
 * CONDITIONS OF ANY KIND, either  express  or  implied.  See  the  License  for  the
 * specific language governing permissions and limitations under the License.
 ************************************************************************************
 */
package org.openbravo.data;

import java.lang.reflect.Field;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.text.SimpleDateFormat;
import java.util.Date;

public class UtilSql {

  public boolean GetStringResultSet(ResultSet result, Object sqlObject) {
    Field f = null;
    try {
      ResultSetMetaData rmeta = result.getMetaData();
      int numColumns = rmeta.getColumnCount();

      for (int i = 1; i <= numColumns; ++i) {
        String strNombreColumna = rmeta.getColumnName(i);
        String strFieldName = TransformaNombreColumna(strNombreColumna);
        f = sqlObject.getClass().getField(strFieldName);
        f.setAccessible(true);

        switch (rmeta.getColumnType(i)) {
        case 2:
          f.set(sqlObject, Long.toString(result.getLong(strNombreColumna)));
          break;
        case 12:
          f.set(sqlObject, result.getString(strNombreColumna));
          break;
        case 93:
          f.set(sqlObject, result.getDate(strNombreColumna));
          if (result.wasNull())
            f.set(sqlObject, new java.sql.Date(System.currentTimeMillis()));
          break;
        }
        if (result.wasNull() && rmeta.getColumnType(i) != 93)
          f.set(sqlObject, "");

        f.setAccessible(false);
      }
    } catch (Exception e) {
      e.printStackTrace();
      return (false);
    }
    return (true);
  }

  public String TransformaNombreColumna(String strColumn) {
    String strNombreTransformado = "";
    Character BarraBaja = new Character('_');
    int intNumCaracteres = strColumn.length();
    boolean blnFueBarraBaja = false;
    for (int i = 0; i < intNumCaracteres; i++) {
      if (i == 0)
        strNombreTransformado = new Character(Character.toUpperCase(strColumn.charAt(i)))
            .toString();
      else {
        if (new Character(strColumn.charAt(i)).compareTo(BarraBaja) == 0)
          blnFueBarraBaja = true;
        else {
          if (blnFueBarraBaja) {
            strNombreTransformado = strNombreTransformado
                + new Character(Character.toUpperCase(strColumn.charAt(i))).toString();
            blnFueBarraBaja = false;
          } else
            strNombreTransformado = strNombreTransformado
                + new Character(Character.toLowerCase(strColumn.charAt(i))).toString();
        }
      }
    }
    return (strNombreTransformado);
  }

  public boolean AsignarValoresEntrada(PreparedStatement ps, int posicion, int tipo, String strValor) {
    try {
      if (strValor != null) {
        if (strValor.compareTo("") == 0)
          ps.setNull(posicion, tipo);
        else {
          switch (tipo) {
          case 2:
            ps.setLong(posicion, Long.valueOf(strValor).longValue());
            break;
          case 12:
            ps.setString(posicion, strValor);
            break;
          }

        }
      } else
        ps.setNull(posicion, tipo);
    } catch (Exception e) {
      e.printStackTrace();
      return (false);
    }
    return (true);
  }

  // setValue and getValue method to be used in sqlc

  public static boolean setValue(PreparedStatement ps, int posicion, int tipo, String strDefault,
      String strValor) {
    try {
      if (strValor == null) {
        strValor = strDefault;
      }
      if (strValor != null) {
        if (strValor.compareTo("") == 0)
          ps.setNull(posicion, tipo);
        else {
          switch (tipo) {
          case 2:
            ps.setLong(posicion, Long.valueOf(strValor).longValue());
            break;
          case 12:
            ps.setString(posicion, strValor);
            break;
          case java.sql.Types.LONGVARCHAR:
            ps.setString(posicion, strValor);
            break;
          case 0:
            ps.setDouble(posicion, Double.valueOf(strValor).doubleValue());
            break;
          }

        }
      } else
        ps.setNull(posicion, tipo);
    } catch (Exception e) {
      e.printStackTrace();
      return (false);
    }
    return (true);
  }

  public static String getValue(ResultSet result, String strField) throws java.sql.SQLException {
    String strValueReturn = result.getString(strField);
    if (result.wasNull())
      strValueReturn = "";
    return strValueReturn;
  }

  public static String getValue(ResultSet result, int pos) throws java.sql.SQLException {
    String strValueReturn = result.getString(pos);
    if (result.wasNull())
      strValueReturn = "";
    return strValueReturn;
  }

  public static String getDateValue(ResultSet result, String strField, String strDateFormat)
      throws java.sql.SQLException {
    // Format the current time.
    String strValueReturn;
    Date date = result.getDate(strField);
    if (result.wasNull()) {
      strValueReturn = "";
    } else {
      // SimpleDateFormat formatter = new SimpleDateFormat ("dd-MM-yyyy");
      SimpleDateFormat formatter = new SimpleDateFormat(strDateFormat);
      strValueReturn = formatter.format(date);
    }
    return strValueReturn;
  }

  public static String getDateValue(ResultSet result, String strField) throws java.sql.SQLException {
    return getDateValue(result, strField, "dd-MM-yyyy");
  }

  public static String getBlobValue(ResultSet result, String strField) throws java.sql.SQLException {
    String strValueReturn = "";
    Blob blob = result.getBlob(strField);
    if (result.wasNull()) {
      strValueReturn = "";
    } else {
      int length = (int) blob.length();
      if (length > 0)
        strValueReturn = new String(blob.getBytes(1, length));
    }
    return strValueReturn;
  }

  public static String getStringCallableStatement(CallableStatement cs, int intField)
      throws java.sql.SQLException {
    String strValueReturn = cs.getString(intField);
    if (strValueReturn == null)
      strValueReturn = "";
    return strValueReturn;
  }
}
