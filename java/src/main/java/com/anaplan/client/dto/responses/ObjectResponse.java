package com.anaplan.client.dto.responses;

/**
 * Created by Spondon Saha
 * User: spondonsaha
 * Date: 7/5/17
 * Time: 1:46 PM
 */
public abstract class ObjectResponse<T> extends BaseResponse {
    public abstract T getItem();

    public abstract void setItem(T item);
}
