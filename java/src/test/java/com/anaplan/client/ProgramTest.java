package com.anaplan.client;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

/**
 * Created by Spondon Saha
 * User: spondonsaha
 * Date: 8/8/17
 * Time: 7:50 AM
 */
public class ProgramTest {

    Program client;

    @Before
    public void setUp() {
        client = new MockProgram();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChunkSizeBad1() {
        MockProgram.fetchChunkSize("asdasd213");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChunkSizeBad2() {
        MockProgram.fetchChunkSize("51012321");
    }

    @Test
    public void testChunkSizeGood() {
        assertEquals(25 * 1000 * 1000, MockProgram.fetchChunkSize("25"));
    }
}
