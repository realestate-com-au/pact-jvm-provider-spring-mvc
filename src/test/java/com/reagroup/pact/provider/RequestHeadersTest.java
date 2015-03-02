package com.reagroup.pact.provider;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import sample.MyController;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:applicationContextPactTest.xml"})
public class RequestHeadersTest {

    @Autowired
    private MyController myController;

    @Test
    public void shouldResponseCorrectlyIfRequestHeadersMatchExactly() throws Exception {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("header1", "value1");
        headers.put("header2", "value2");
        testWithHeaders(headers);
    }

    @Test
    public void keysAreCaseInsensitive() throws Exception {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("HEADER1", "value1");
        headers.put("HEADER2", "value2");
        testWithHeaders(headers);
    }

    @Test
    public void extraKeysAreIgnored() throws Exception {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("header1", "value1");
        headers.put("header2", "value2");
        headers.put("extra-header-not-used-by-controller", "but-it-should-be-ok");
        testWithHeaders(headers);
    }

    private void testWithHeaders(Map<String, String> headers) throws Exception {
        MockHttpServletRequestBuilder builder = RequestBuilder.path("/hello/headers").headers(headers).build();
        ServerBuilder.build(myController)
                .perform(builder)
                .andExpect(status().is(HttpStatus.OK.value()))
                .andExpect(content().string("value1+value2"));
    }


}

