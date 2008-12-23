/**
 * WebServiceImpl.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.openbravo.services.webservice;

public interface WebServiceImpl extends java.rmi.Remote {
    public byte[] getModule(java.lang.String in0)
            throws java.rmi.RemoteException;

    public org.openbravo.services.webservice.Customer[] getCustomers(int in0,
            java.lang.String in1, java.lang.String in2)
            throws java.rmi.RemoteException;

    public org.openbravo.services.webservice.Customer getCustomer(int in0,
            int in1, java.lang.String in2, java.lang.String in3)
            throws java.rmi.RemoteException;

    public org.openbravo.services.webservice.Customer getCustomer(int in0,
            java.lang.String in1, java.lang.String in2, java.lang.String in3,
            java.lang.String in4) throws java.rmi.RemoteException;

    public java.lang.Boolean updateCustomer(
            org.openbravo.services.webservice.BusinessPartner in0,
            java.lang.String in1, java.lang.String in2)
            throws java.rmi.RemoteException;

    public int[] getCustomerAddresses(int in0, int in1, java.lang.String in2,
            java.lang.String in3) throws java.rmi.RemoteException;

    public org.openbravo.services.webservice.Location getCustomerLocation(
            int in0, int in1, int in2, java.lang.String in3,
            java.lang.String in4) throws java.rmi.RemoteException;

    public java.lang.Boolean updateAddress(
            org.openbravo.services.webservice.Location in0,
            java.lang.String in1, java.lang.String in2)
            throws java.rmi.RemoteException;

    public org.openbravo.services.webservice.Contact getCustomerContact(
            int in0, int in1, int in2, java.lang.String in3,
            java.lang.String in4) throws java.rmi.RemoteException;

    public java.lang.Boolean updateContact(
            org.openbravo.services.webservice.Contact in0,
            java.lang.String in1, java.lang.String in2)
            throws java.rmi.RemoteException;

    public org.openbravo.services.webservice.SimpleModule[] moduleSearch(
            java.lang.String in0, java.lang.String[] in1)
            throws java.rmi.RemoteException;

    public org.openbravo.services.webservice.Module moduleDetail(
            java.lang.String in0) throws java.rmi.RemoteException;

    public org.openbravo.services.webservice.SimpleModule[] moduleScanForUpdates(
            java.util.HashMap in0) throws java.rmi.RemoteException;

    public org.openbravo.services.webservice.Module moduleRegister(
            org.openbravo.services.webservice.Module in0, java.lang.String in1,
            java.lang.String in2) throws java.rmi.RemoteException;

    public org.openbravo.services.webservice.ModuleInstallDetail checkConsistency(
            java.util.HashMap in0, java.lang.String[] in1,
            java.lang.String[] in2) throws java.rmi.RemoteException;
}
