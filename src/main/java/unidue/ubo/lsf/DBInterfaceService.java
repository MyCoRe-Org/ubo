/**
 * Copyright (c) 2016 Duisburg-Essen University Library
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 **/

package unidue.ubo.lsf;

public interface DBInterfaceService extends javax.xml.rpc.Service {
    public java.lang.String getdbinterfaceAddress();

    public unidue.ubo.lsf.DBInterface getdbinterface() throws javax.xml.rpc.ServiceException;

    public unidue.ubo.lsf.DBInterface getdbinterface(java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
}
