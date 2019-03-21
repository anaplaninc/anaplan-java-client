package com.anaplan.client.dto.responses;
import com.anaplan.client.dto.UserData;

/**
 * Logged in User response
 */
public class UserResponse extends ObjectResponse<UserData> {

    private UserData user;

    @Override
    public UserData getItem() {
        return user;
    }

    @Override
    public void setItem(UserData item) {
        this.user = item;
    }
}
