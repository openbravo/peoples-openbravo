package org.openbravo.retail.posterminal;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.client.kernel.BaseKernelServlet;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.service.json.DataResolvingMode;
import org.openbravo.service.json.DataToJsonConverter;

/**
 * @author iperdomo
 * 
 */
public class DataSourceServlet extends BaseKernelServlet {

  private static final long serialVersionUID = 1L;
  private static final Logger log = Logger.getLogger(DataSourceServlet.class);

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {

    int idx = 0;
    final long t1 = System.currentTimeMillis();
    final PrintWriter out = response.getWriter();
    final DataToJsonConverter converter = new DataToJsonConverter();

    response.setContentType("application/json");
    out.println("{\"data\": [");
    out.flush();

    final OBQuery<BaseOBObject> obq = OBDal.getInstance().createQuery("Product", "order by name");
    final ScrollableResults results = obq.scroll(ScrollMode.FORWARD_ONLY);

    try {
      while (results.next()) {
        if (idx > 0) {
          out.print(",");
        }

        final BaseOBObject obj = (BaseOBObject) results.get(0);
        out.println(converter.toJsonObject(obj, DataResolvingMode.SHORT).toString());
        idx++;
        if (idx % 100 == 0) {
          out.flush();
          OBDal.getInstance().getSession().clear();
        }
      }
    } finally {
      results.close();
      OBDal.getInstance().getSession().clear(); // always clear the session (eg. result < 100 rows)
    }
    out.print("]}");
    out.close();
    log.debug("total fetch time: " + (System.currentTimeMillis() - t1) + "ms");
  }
}
