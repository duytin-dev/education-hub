package com.iTech.education.websocket;

import java.security.Principal;

public class ChatPrincipal implements Principal {

    private final String name;
    private final boolean guest;
    private final boolean staff;

    public ChatPrincipal(String name, boolean guest, boolean staff) {
        this.name = name;
        this.guest = guest;
        this.staff = staff;
    }

    @Override
    public String getName() {
        return name;
    }

    public boolean isGuest() {
        return guest;
    }

    public boolean isStaff() {
        return staff;
    }

    public String guestToken() {
        return guest ? name : null;
    }

    public String email() {
        return guest ? null : name;
    }
}
