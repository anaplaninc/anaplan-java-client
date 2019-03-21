package com.anaplan.client.dto;

import java.util.Date;


/**
 * Logged in User response
 */
public class UserData {
    private String id;
    private boolean active;
    private String email;
    private String emailOptIn;
    private String firstName;
    private String lastName;
    private Date lastLoginDate;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmailOptIn() {
        return emailOptIn;
    }

    public void setEmailOptIn(String emailOptIn) {
        this.emailOptIn = emailOptIn;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Date getLastLoginDate() {
        return lastLoginDate;
    }

    public void setLastLoginDate(Date lastLoginDate) {
        this.lastLoginDate = lastLoginDate;
    }
}
