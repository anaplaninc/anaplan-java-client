package com.anaplan.client.ex;

/**
 * Created by Spondon Saha
 * Date: 4/26/18
 * Time: 5:34 PM
 */
public class CreateImportDatasourceError extends RuntimeException {
    public CreateImportDatasourceError(String importDatasourceName) {
        super("Could not create import-data with ID: " + importDatasourceName);
    }
}
