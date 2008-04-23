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
 * The Initial Developer of the Original Code is Openbravo SL 
 * All portions are Copyright (C) 2008 Openbravo SL 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
*/

package org.openbravo.erpCommon.ws.services;

import org.openbravo.database.ConnectionProvider;
import org.openbravo.base.ConnectionProviderContextListener;

import org.apache.log4j.Logger ;

import org.apache.axis.MessageContext;
import org.apache.axis.transport.http.HTTPConstants;

import javax.servlet.http.HttpServlet;
import javax.servlet.ServletContext;

public class WebServiceImpl implements WebService {
	protected static ConnectionProvider pool;	
    static Logger log4j = Logger.getLogger(WebService.class);
    
	public WebServiceImpl() {
		initPool();
	}
	
	private boolean access(String username, String password) {
     try {
      	return !WebServicesData.hasAccess(pool, username,password).equals("0");
      } catch (Exception e) {
        return false;
      }      
    }
	
	public Customer[] getCustomers(int clientId, String username, String password) {
		Customer[] customers = null;
		if (!access(username, password)) {
	      if (log4j.isDebugEnabled()) log4j.debug("Access denied for user: " + username); 
	       return null;
	    }
		try {
			WebServicesCustomerData[] data = WebServicesCustomerData.select(pool, Integer.toString(clientId));
			customers = new Customer[data.length];

			for(int i=0; i < data.length; i++) {
				customers[i] = new Customer();
				customers[i].setId(Integer.valueOf(data[i].id).intValue());
				customers[i].setClientId(Integer.valueOf(data[i].clientId).intValue());
				customers[i].setName(data[i].name);
				customers[i].setDescription(data[i].description);
				customers[i].setSearchKey(data[i].searchkey);
				customers[i].setComplete(false);
			}
		}
		catch(Exception e) {
			log4j.error(e.getMessage());
		}
		finally {
			destroyPool();
		}
		return customers;
	}
	
	public Customer getCustomer(int clientId, int customerId, String username, String password) {
		Customer customer = null;
		if (!access(username, password)) {
	      if (log4j.isDebugEnabled()) log4j.debug("Access denied for user: " + username); 
	       return null;
	    }
		try {
			
			WebServicesCustomerData[] data = WebServicesCustomerData.selectCustomerById(pool, String.valueOf(clientId), String.valueOf(customerId));

			if(data.length > 0) {
				customer = new Customer();
				customer.setId(Integer.valueOf(data[0].id).intValue());
				customer.setClientId(Integer.valueOf(data[0].clientId).intValue());
				customer.setName(data[0].name);
				customer.setSearchKey(data[0].searchkey);
				customer.setComplete(false);
			}
		}
		catch(Exception e) {
			log4j.error(e.getMessage());
		}
		finally {
			destroyPool();
		}
		return customer;
	}
	
	public Customer getCustomer(int clientId, String name, String searchKey, String username, String password) {
		Customer customer = null;
		if (!access(username, password)) {
	      if (log4j.isDebugEnabled()) log4j.debug("Access denied for user: " + username); 
	       return null;
	    }
		try {
			
			WebServicesCustomerData[] data = WebServicesCustomerData.selectCustomer(pool, String.valueOf(clientId), name, searchKey);

			if(data.length == 1) {
				customer = new Customer();
				customer.setId(Integer.valueOf(data[0].id).intValue());
				customer.setClientId(Integer.valueOf(data[0].clientId).intValue());
				customer.setName(data[0].name);
				customer.setSearchKey(data[0].searchkey);
				customer.setComplete(false);
			}
		}
		catch(Exception e) {
			log4j.error(e.getMessage());
		}
		finally {
			destroyPool();
		}
		return customer;
	}
	
	public Boolean updateCustomer(BusinessPartner customer, String username, String password) {
		int updated = 0;
		if (!access(username, password)) {
	      if (log4j.isDebugEnabled()) log4j.debug("Access denied for user: " + username); 
	       return null;
	    }
		try {
			updated = WebServicesCustomerData.updateCustomer(pool, customer.getName(), customer.getSearchKey(), String.valueOf(customer.getClientId()), String.valueOf(customer.getId()));			
		}
		catch(Exception e) {
			log4j.error(e.getMessage());
		}
		finally {
			destroyPool();
		}
		return (updated == 0 ? false : true);
	}
	
	public int[] getCustomerAddresses(int clientId, int customerId, String username, String password) {
		int[] locationList = null;
		if (!access(username, password)) {
	      if (log4j.isDebugEnabled()) log4j.debug("Access denied for user: " + username); 
	       return null;
	    }
		try {
			WebServicesAddressData[] data = WebServicesAddressData.selectLocationList(pool, String.valueOf(clientId), String.valueOf(customerId));
			locationList = new int[data.length];
			for(int i=0; i < data.length; i++) {
				locationList[i] = Integer.valueOf(data[i].cLocationId).intValue();
			}
		}
		catch(Exception e) {
			log4j.error(e.getMessage());
		}
		finally {
			destroyPool();
		}
		return locationList;
	}
	
	public Location getCustomerLocation(int clientId, int customerId, int locationId, String username, String password) {
		Location customerLocation = null;
		if (!access(username, password)) {
	      if (log4j.isDebugEnabled()) log4j.debug("Access denied for user: " + username); 
	       return null;
	    }
		try {
			WebServicesAddressData[] data = WebServicesAddressData.select(pool, String.valueOf(clientId), String.valueOf(customerId), String.valueOf(locationId));
			if(data.length > 0) {
				customerLocation = new Location();
				customerLocation.setId(Integer.valueOf(data[0].cLocationId).intValue());
				customerLocation.setClientId(Integer.valueOf(data[0].adClientId).intValue());
				customerLocation.setBusinessPartnerId(Integer.valueOf(data[0].cBpartnerId).intValue());
				customerLocation.setAddress1(data[0].address1);
				customerLocation.setAddress2(data[0].address2);
				customerLocation.setCity(data[0].city);
				customerLocation.setPostal(data[0].postal);
				customerLocation.setRegion(data[0].region);
				customerLocation.setCountry(data[0].country);
			}
		}
		catch(Exception e) {
			log4j.error(e.getMessage());
		}
		finally {
			destroyPool();
		}
		return customerLocation;
	}
	
	public Boolean updateAddress(Location addr, String username, String password) {
		int updated = 0;
		try {
			updated = WebServicesAddressData.updateAddress(pool, addr.getAddress1(), addr.getAddress2(), addr.getCity(), addr.getPostal(), String.valueOf(addr.getClientId()), String.valueOf(addr.getId()));
		}
		catch(Exception e) {
			log4j.error(e.getMessage());
		}
		finally {
			destroyPool();
		}
		return (updated == 0 ? false : true);
	}
	
	public Contact getCustomerContact(int clientId, int customerId, int contactId, String username, String password) {
		Contact customerContact = null;
		if (!access(username, password)) {
	      if (log4j.isDebugEnabled()) log4j.debug("Access denied for user: " + username); 
	       return null;
	    }
		try {
			WebServicesContactData[] data = WebServicesContactData.select(pool, String.valueOf(clientId), String.valueOf(customerId), String.valueOf(contactId));
			if(data.length > 0) {
				customerContact = new Contact();
				customerContact.setId(Integer.valueOf(data[0].adUserId).intValue());
				customerContact.setClientId(Integer.valueOf(data[0].adClientId).intValue());
				customerContact.setBusinessPartnerId(Integer.valueOf(data[0].cBpartnerId).intValue());
				customerContact.setFirstName(data[0].firstname);
				customerContact.setLastName(data[0].lastname);
				customerContact.setPhone(data[0].phone);
				customerContact.setPhone2(data[0].phone2);
				customerContact.setEmail(data[0].emailuser);
				customerContact.setFax(data[0].fax);
			}
		}
		catch(Exception e) {
			log4j.error(e.getMessage());
		}
		finally {
			destroyPool();
		}
		return customerContact;
	}

	public Boolean updateContact(Contact contact, String username, String password) {
		int updated = 0;
		try {
			updated = WebServicesContactData.updateContact(pool, contact.getFirstName(), contact.getLastName(), contact.getEmail(), contact.getPhone(), contact.getPhone2(), contact.getFax(), String.valueOf(contact.getClientId()), String.valueOf(contact.getBusinessPartnerId()), String.valueOf(contact.getId()));
		}
		catch(Exception e) {
			log4j.error(e.getMessage());
		}
		finally{
			destroyPool();
		}
		return (updated == 0 ? false : true);
	}
	
	private void initPool () {
      if (log4j.isDebugEnabled()) log4j.debug("init");
       try{
    	   HttpServlet srv = (HttpServlet) MessageContext.getCurrentContext().getProperty(HTTPConstants.MC_HTTP_SERVLET);
           ServletContext context = srv.getServletContext();
           pool = ConnectionProviderContextListener.getPool(context);
       } catch (Exception e) {
          log4j.error("Error : initPool");
          log4j.error(e.getStackTrace());
       }
    }

	private void destroyPool() {
      if (log4j.isDebugEnabled()) log4j.debug("destroy");
    }
}
