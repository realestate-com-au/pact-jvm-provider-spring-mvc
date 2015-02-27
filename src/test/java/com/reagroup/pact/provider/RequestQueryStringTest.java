package com.reagroup.pact.provider;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import sample.MyController;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:applicationContextPactTest.xml"})
public class RequestQueryStringTest {

    @Autowired
    private MyController myController;

    @Test
    public void shouldResponseCorrectlyIfRequestQueryStringMatchesExactly() throws Exception {
        MockHttpServletRequestBuilder builder = RequestBuilder.path("/hello/querystring").query("name=pact&phones=111111&phones=222222").build();
        ServerBuilder.build(myController)
                .perform(builder)
                .andExpect(status().is(HttpStatus.OK.value()))
                .andExpect(content().string("good"));
    }

    @Test
    public void ordersOfQueryStringKeysAreNotMatter() throws Exception {
        MockHttpServletRequestBuilder builder = RequestBuilder.path("/hello/querystring").query("phones=111111&phones=222222&name=pact").build();
        ServerBuilder.build(myController)
                .perform(builder)
                .andExpect(status().is(HttpStatus.OK.value()))
                .andExpect(content().string("good"));
    }

    @Test
    public void ordersOfValuesOfTheSameKeyMatter() throws Exception {
        MockHttpServletRequestBuilder builder = RequestBuilder.path("/hello/querystring").query("phones=222222&phones=111111&name=pact").build();
        ServerBuilder.build(myController)
                .perform(builder)
                .andExpect(status().is(HttpStatus.OK.value()))
                .andExpect(content().string("unknown"));
    }

}

