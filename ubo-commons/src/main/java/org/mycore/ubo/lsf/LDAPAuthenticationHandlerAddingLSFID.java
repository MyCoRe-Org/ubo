package org.mycore.ubo.lsf;

import org.mycore.user2.MCRUser;

import org.mycore.ubo.login.LDAPAuthenticationHandler;

public class LDAPAuthenticationHandlerAddingLSFID extends LDAPAuthenticationHandler {

    public MCRUser authenticate(String uid, String pwd) throws Exception {
        MCRUser user = super.authenticate(uid, pwd);
        if (user != null)
            User2LSFHelper.discoverLSFIDof(user);
        return user;
    }
}
