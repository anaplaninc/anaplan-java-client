package com.anaplan.client;

import com.anaplan.client.dto.responses.ModelsResponse;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Created by Spondon Saha
 * User: spondonsaha
 * Date: 6/27/17
 * Time: 6:43 PM
 */
public class PaginatorTest extends BaseTest {

    private Model mockModel;

    @Before
    public void setUp() throws IOException {
        mockModel = fetchMockModel();
    }

    @Test
    public void testMultipleResults() throws Exception {
        when(mockModel.getApi().getModels(
                getTestUserId(), 0))
                .thenReturn(createFeignResponse("responses/page1_response.json", ModelsResponse.class));
        when(mockModel.getApi().getModels(
                getTestUserId(), 4))
                .thenReturn(createFeignResponse("responses/page2_response.json", ModelsResponse.class));
        Workspace testWorkspace = getTestWorkspace();
        List<Model> models = new ArrayList<>();
        testWorkspace.getModels().forEach(models::add);
        assertEquals(6, models.size());
    }

    @Test
    public void testEmptyResponse() throws Exception {
        Workspace testWorkspace = getTestWorkspace();
        when(mockModel.getApi().getModels(
                getTestUserId(), 0))
                .thenReturn(createFeignResponse("responses/empty_response.json", ModelsResponse.class));
        List<Model> models = Lists.newArrayList(testWorkspace.getModels());
        assertEquals(0, models.size());
    }
}
