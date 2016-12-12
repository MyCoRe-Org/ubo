/**
 * Copyright (c) 2016 Duisburg-Essen University Library
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 **/

package unidue.ubo.lsf;

public class DBInterfaceServiceLocator extends org.apache.axis.client.Service implements unidue.ubo.lsf.DBInterfaceService {

    public DBInterfaceServiceLocator() {
    }

    public DBInterfaceServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public DBInterfaceServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for dbinterface
    private java.lang.String dbinterface_address = "http://www.lsf.uni-due.de/lsf/services/dbinterface";

    public java.lang.String getdbinterfaceAddress() {
        return dbinterface_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String dbinterfaceWSDDServiceName = "dbinterface";

    public java.lang.String getdbinterfaceWSDDServiceName() {
        return dbinterfaceWSDDServiceName;
    }

    public void setdbinterfaceWSDDServiceName(java.lang.String name) {
        dbinterfaceWSDDServiceName = name;
    }

    public unidue.ubo.lsf.DBInterface getdbinterface() throws javax.xml.rpc.ServiceException {
        java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(dbinterface_address);
        } catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getdbinterface(endpoint);
    }

    public unidue.ubo.lsf.DBInterface getdbinterface(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            unidue.ubo.lsf.DbinterfaceSoapBindingStub _stub = new unidue.ubo.lsf.DbinterfaceSoapBindingStub(portAddress, this);
            _stub.setPortName(getdbinterfaceWSDDServiceName());
            return _stub;
        } catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setdbinterfaceEndpointAddress(java.lang.String address) {
        dbinterface_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (unidue.ubo.lsf.DBInterface.class.isAssignableFrom(serviceEndpointInterface)) {
                unidue.ubo.lsf.DbinterfaceSoapBindingStub _stub = new unidue.ubo.lsf.DbinterfaceSoapBindingStub(new java.net.URL(
                        dbinterface_address), this);
                _stub.setPortName(getdbinterfaceWSDDServiceName());
                return _stub;
            }
        } catch (java.lang.Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  "
                + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface)
            throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        java.lang.String inputPortName = portName.getLocalPart();
        if ("dbinterface".equals(inputPortName)) {
            return getdbinterface();
        } else {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://www.lsf.uni-due.de/lsf/services/dbinterface", "DBInterfaceService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://www.lsf.uni-due.de/lsf/services/dbinterface", "dbinterface"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {

        if ("dbinterface".equals(portName)) {
            setdbinterfaceEndpointAddress(address);
        } else { // Unknown Port Name
            throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(javax.xml.namespace.QName portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }

}
