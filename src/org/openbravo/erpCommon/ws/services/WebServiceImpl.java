/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.0  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html 
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License. 
 * The Original Code is Openbravo ERP. 
 * The Initial Developer of the Original Code is Openbravo SLU 
 * All portions are Copyright (C) 2008 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.erpCommon.ws.services;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;

import org.apache.axis.MessageContext;
import org.apache.axis.transport.http.HTTPConstants;
import org.apache.log4j.Logger;
import org.openbravo.base.ConnectionProviderContextListener;
import org.openbravo.database.ConnectionProvider;

public class WebServiceImpl implements WebService {
  protected static ConnectionProvider pool;
  static Logger log4j = Logger.getLogger(WebService.class);

  public WebServiceImpl() {
    initPool();
  }

  private boolean access(String username, String password) {
    try {
      return !WebServicesData.hasAccess(pool, username, password).equals("0");
    } catch (Exception e) {
      return false;
    }
  }

  public Customer[] getCustomers(String clientId, String username, String password) {
    Customer[] customers = null;
    if (!access(username, password)) {
      if (log4j.isDebugEnabled())
        log4j.debug("Access denied for user: " + username);
      return null;
    }
    try {

      WebServicesCustomerData[] data = WebServicesCustomerData.select(pool, clientId, username);

      customers = new Customer[data.length];

      for (int i = 0; i < data.length; i++) {
        customers[i] = new Customer();
        customers[i].setId(data[i].id);
        customers[i].setClientId(data[i].clientId);
        customers[i].setName(data[i].name);
        customers[i].setDescription(data[i].description);
        customers[i].setSearchKey(data[i].searchkey);
        customers[i].setComplete(false);
      }
    } catch (Exception e) {
      log4j.error(e.getMessage());
    } finally {
      destroyPool();
    }
    return customers;
  }

  public Customer getCustomer(String clientId, String customerId, String username, String password) {
    Customer customer = null;
    if (!access(username, password)) {
      if (log4j.isDebugEnabled())
        log4j.debug("Access denied for user: " + username);
      return null;
    }
    try {

      WebServicesCustomerData[] data = WebServicesCustomerData.selectCustomerById(pool, clientId,
          customerId, username);

      if (data.length > 0) {
        customer = new Customer();
        customer.setId(data[0].id);
        customer.setClientId(data[0].clientId);
        customer.setName(data[0].name);
        customer.setSearchKey(data[0].searchkey);
        customer.setComplete(false);
      }
    } catch (Exception e) {
      log4j.error(e.getMessage());
    } finally {
      destroyPool();
    }
    return customer;
  }

  public Customer getCustomer(String clientId, String name, String searchKey, String username,
      String password) {
    Customer customer = null;
    if (!access(username, password)) {
      if (log4j.isDebugEnabled())
        log4j.debug("Access denied for user: " + username);
      return null;
    }
    try {

      WebServicesCustomerData[] data = WebServicesCustomerData.selectCustomer(pool, clientId,
          username, (name == null ? "" : name), (searchKey == null ? "" : searchKey));

      if (data.length == 1) {
        customer = new Customer();
        customer.setId(data[0].id);
        customer.setClientId(data[0].clientId);
        customer.setName(data[0].name);
        customer.setSearchKey(data[0].searchkey);
        customer.setComplete(false);
      }
    } catch (Exception e) {
      log4j.error(e.getMessage());
    } finally {
      destroyPool();
    }
    return customer;
  }

  public Boolean updateCustomer(BusinessPartner customer, String username, String password) {
    int updated = 0;
    if (!access(username, password)) {
      if (log4j.isDebugEnabled())
        log4j.debug("Access denied for user: " + username);
      return null;
    }
    try {
      updated = WebServicesCustomerData.updateCustomer(pool, customer.getName(), customer
          .getSearchKey(), String.valueOf(customer.getClientId()),
          String.valueOf(customer.getId()), username);
    } catch (Exception e) {
      log4j.error(e.getMessage());
    } finally {
      destroyPool();
    }
    return (updated == 0 ? false : true);
  }

  public int[] getCustomerAddresses(String clientId, String customerId, String username,
      String password) {
    int[] locationList = null;
    if (!access(username, password)) {
      if (log4j.isDebugEnabled())
        log4j.debug("Access denied for user: " + username);
      return null;
    }
    try {
      WebServicesAddressData[] data = WebServicesAddressData.selectLocationList(pool, clientId,
          customerId);
      locationList = new int[data.length];
      for (int i = 0; i < data.length; i++) {
        locationList[i] = Integer.valueOf(data[i].cLocationId).intValue();
      }
    } catch (Exception e) {
      log4j.error(e.getMessage());
    } finally {
      destroyPool();
    }
    return locationList;
  }

  public Location getCustomerLocation(String clientId, String customerId, String locationId,
      String username, String password) {
    Location customerLocation = null;
    if (!access(username, password)) {
      if (log4j.isDebugEnabled())
        log4j.debug("Access denied for user: " + username);
      return null;
    }
    try {
      WebServicesAddressData[] data = WebServicesAddressData.select(pool, clientId, customerId,
          locationId);
      if (data.length > 0) {
        customerLocation = new Location();
        customerLocation.setId(data[0].cLocationId);
        customerLocation.setClientId(data[0].adClientId);
        customerLocation.setBusinessPartnerId(data[0].cBpartnerId);
        customerLocation.setAddress1(data[0].address1);
        customerLocation.setAddress2(data[0].address2);
        customerLocation.setCity(data[0].city);
        customerLocation.setPostal(data[0].postal);
        customerLocation.setRegion(data[0].region);
        customerLocation.setCountry(data[0].country);
      }
    } catch (Exception e) {
      log4j.error(e.getMessage());
    } finally {
      destroyPool();
    }
    return customerLocation;
  }

  public Boolean updateAddress(Location addr, String username, String password) {
    int updated = 0;
    try {
      updated = WebServicesAddressData.updateAddress(pool, addr.getAddress1(), addr.getAddress2(),
          addr.getCity(), addr.getPostal(), String.valueOf(addr.getClientId()), String.valueOf(addr
              .getId()));
    } catch (Exception e) {
      log4j.error(e.getMessage());
    } finally {
      destroyPool();
    }
    return (updated == 0 ? false : true);
  }

  public Contact getCustomerContact(String clientId, String customerId, String contactId,
      String username, String password) {
    Contact customerContact = null;
    if (!access(username, password)) {
      if (log4j.isDebugEnabled())
        log4j.debug("Access denied for user: " + username);
      return null;
    }
    try {
      WebServicesContactData[] data = WebServicesContactData.select(pool, clientId, customerId,
          contactId);
      if (data.length > 0) {
        customerContact = new Contact();
        customerContact.setId(data[0].adUserId);
        customerContact.setClientId(data[0].adClientId);
        customerContact.setBusinessPartnerId(data[0].cBpartnerId);
        customerContact.setFirstName(data[0].firstname);
        customerContact.setLastName(data[0].lastname);
        customerContact.setPhone(data[0].phone);
        customerContact.setPhone2(data[0].phone2);
        customerContact.setEmail(data[0].email);
        customerContact.setFax(data[0].fax);
      }
    } catch (Exception e) {
      log4j.error(e.getMessage());
    } finally {
      destroyPool();
    }
    return customerContact;
  }

  public Boolean updateContact(Contact contact, String username, String password) {
    int updated = 0;
    try {
      updated = WebServicesContactData.updateContact(pool, contact.getFirstName(), contact
          .getLastName(), contact.getEmail(), contact.getPhone(), contact.getPhone2(), contact
          .getFax(), String.valueOf(contact.getClientId()), String.valueOf(contact
          .getBusinessPartnerId()), String.valueOf(contact.getId()));
    } catch (Exception e) {
      log4j.error(e.getMessage());
    } finally {
      destroyPool();
    }
    return (updated == 0 ? false : true);
  }

  private void initPool() {
    if (log4j.isDebugEnabled())
      log4j.debug("init");
    try {
      HttpServlet srv = (HttpServlet) MessageContext.getCurrentContext().getProperty(
          HTTPConstants.MC_HTTP_SERVLET);
      ServletContext context = srv.getServletContext();
      pool = ConnectionProviderContextListener.getPool(context);
    } catch (Exception e) {
      log4j.error("Error : initPool");
      log4j.error(e.getStackTrace());
    }
  }

  private void destroyPool() {
    if (log4j.isDebugEnabled())
      log4j.debug("destroy");
  }
}
