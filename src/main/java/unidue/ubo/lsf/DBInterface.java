/**
 * Copyright (c) 2016 Duisburg-Essen University Library
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 **/

package unidue.ubo.lsf;

public interface DBInterface extends java.rmi.Remote {
    public java.lang.String getDataByParams(java.lang.String strName, java.lang.String xmlParams) throws java.rmi.RemoteException;

    public java.lang.String getDataByParams(java.lang.String strName, java.lang.String strID, java.lang.String xmlParams)
            throws java.rmi.RemoteException;

    public java.lang.String getDataXML(java.lang.String xmlParams) throws java.rmi.RemoteException;

    public java.lang.String getDataXMLByAuthUser(java.lang.String xmlParams) throws java.rmi.RemoteException;

    public java.lang.String getDataSearch(java.lang.String elParams) throws java.rmi.RemoteException;

    public java.lang.String process(java.lang.String system, java.lang.String body) throws java.rmi.RemoteException;

    public java.lang.String process(java.lang.String system, java.lang.String user, java.lang.String pass, java.lang.String body)
            throws java.rmi.RemoteException;

    public java.lang.String getData(java.lang.String strName, java.lang.String strID) throws java.rmi.RemoteException;
}
