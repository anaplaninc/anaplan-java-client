package com.anaplan.client;

import com.anaplan.client.dto.ListItemParametersData;
import com.anaplan.client.dto.ListItemResultData;
import java.util.List;
import java.util.Map;
/**
 * A named entity in an Anaplan model that can initiate CRUD actions for list item on the server.
 */
public abstract class ListFactory {

  /**
   * Constructor
   */
  protected ListFactory() {
  }

  /**
   * Add an item to list
   * @param listItemParametersData {@link ListItemParametersData}
   * @return {@link ListItemResultData}
   */
  abstract ListItemResultData addItemsToList(final ListItemParametersData listItemParametersData);

  /**
   * Update items from list
   * @param listItemParametersData {@link ListItemParametersData}
   * @return {@link ListItemResultData}
   */
  abstract ListItemResultData updateItemsList(final ListItemParametersData listItemParametersData);

  /**
   * Remove items from list
   * @param itemParametersData {@link ListItemParametersData}
   * @return {@link ListItemResultData}
   */
  abstract ListItemResultData deleteItemsList(final ListItemParametersData itemParametersData);

  /**
   * Remove items from list
   * @param rows items
   * @param header header from source
   * @param headerMap header name mapped
   * @return {@link ListItemResultData}
   */
  abstract ListItemResultData deleteItemsList(final List<String[]> rows, final String[] header, final Map<String, String> headerMap);
}
