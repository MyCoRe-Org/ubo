/**
 * $Revision$ 
 * $Date$
 *
 * This file is part of the MILESS repository software.
 * Copyright (C) 2011 MILESS/MyCoRe developer team
 * See http://duepublico.uni-duisburg-essen.de/ and http://www.mycore.de/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
