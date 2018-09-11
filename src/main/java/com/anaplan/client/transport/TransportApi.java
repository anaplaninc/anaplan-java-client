package com.anaplan.client.transport;

import com.anaplan.client.auth.Credentials;
import com.anaplan.client.api.AnaplanAPI;

/**
 * Created by Spondon Saha
 * Date: 4/17/18
 * Time: 5:16 PM
 */
public interface TransportApi {

    AnaplanAPI getApiClient();

    void setApiClient(AnaplanAPI anaplanAPI);
}
