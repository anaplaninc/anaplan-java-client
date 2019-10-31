package com.anaplan.client.dto.responses;

/**
 * Created by Spondon Saha
 * User: spondonsaha
 * Date: 8/7/17
 * Time: 9:23 AM
 */
public abstract class AuthObjectResponse<T> extends BaseAuthResponse {
    public abstract T getItem();

    public abstract void setItem(T item);
}
