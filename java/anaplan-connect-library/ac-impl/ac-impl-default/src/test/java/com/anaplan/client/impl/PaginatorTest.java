package com.anaplan.client.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.anaplan.client.Model;
import com.anaplan.client.Workspace;
import com.anaplan.client.dto.responses.ModelsResponse;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Created by Ashish Goyal Date: 08/18/20 Time: 6:43 PM
 */
public class PaginatorTest extends BaseTest {

  private Model mockModel;

  @BeforeEach
  public void setUp() throws IOException {
    mockModel = fetchMockModel();
  }

  @Test
  public void testMultipleResults() throws Exception {
    when(mockModel.getApi().getModels(0))
        .thenReturn(createFeignResponse("responses/page1_response.json", ModelsResponse.class));
    when(mockModel.getApi().getModels(4))
        .thenReturn(createFeignResponse("responses/page2_response.json", ModelsResponse.class));
    Workspace testWorkspace = getTestWorkspacebyId();
    List<Model> models = new ArrayList<>();
    testWorkspace.getModels().forEach(models::add);
    assertEquals(7, models.size());
  }

  @Test
  public void testEmptyResponse() throws Exception {
    Workspace testWorkspace = getTestWorkspacebyId();
    when(mockModel.getApi().getModels(0))
        .thenReturn(createFeignResponse("responses/empty_response.json", ModelsResponse.class));
    List<Model> models = Lists.newArrayList(testWorkspace.getModels());
    assertEquals(0, models.size());
  }
}