package com.anaplan.client.ex;

/**
 * Created by Spondon Saha
 * Date: 4/18/18
 * Time: 8:50 PM
 */
public class ModelNotFoundException extends RuntimeException {
    public ModelNotFoundException(String modelGuid, Throwable t) {
        super("Model with Model-Guid not found: " + modelGuid, t);
    }
}
