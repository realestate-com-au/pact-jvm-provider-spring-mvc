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
public class RequestPathTest {

    @Autowired
    private MyController myController;

    @Test
    public void shouldResponseCorrectlyIfRequestPathMatchesExactly() throws Exception {
        MockHttpServletRequestBuilder builder = RequestBuilder.path("/hello/plain").build();
        ServerBuilder.build(myController)
                .perform(builder)
                .andExpect(status().is(HttpStatus.OK.value()))
                .andExpect(content().string("world"));
    }

    @Test
    public void shouldResponseNotFoundIfRequestPathHasDifferentCase() throws Exception {
        MockHttpServletRequestBuilder builder = RequestBuilder.path("/Hello/Plain").build();
        ServerBuilder.build(myController)
                .perform(builder)
                .andExpect(status().is(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    public void shouldResponseNotFoundIfRequestPathHasDifferentEndingSlash() throws Exception {
        MockHttpServletRequestBuilder builder = RequestBuilder.path("/hello/plain/").build();
        ServerBuilder.build(myController)
                .perform(builder)
                .andExpect(status().is(HttpStatus.NOT_FOUND.value()));
    }

}

