package com.anaplan.client.auth;

import feign.FeignException;

/**
 * Created by Spondon Saha
 * User: spondonsaha
 * Date: 2/13/18
 * Time: 4:08 AM
 */
public class MockFeignException extends FeignException {
    public MockFeignException(String message) {
        super(message);
    }
}
