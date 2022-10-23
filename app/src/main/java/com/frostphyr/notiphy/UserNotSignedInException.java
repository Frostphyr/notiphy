package com.frostphyr.notiphy;

import java.security.AccessControlException;

public class UserNotSignedInException extends AccessControlException {

    public UserNotSignedInException() {
        super(null);
    }

}
