package com.reagroup.pact.provider;

import org.junit.Before;
import org.junit.runner.RunWith;

import static org.mockito.Mockito.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import sample.MyControllerWithService;
import sample.MyResponseService;

@RunWith(PactRunner.class)
@ContextConfiguration(locations = {"classpath:applicationContextPactTest.xml"})
@PactFile("file:src/test/resources/consumer-provider.json")
public class PactExampleTest {

    @Autowired
    private MyControllerWithService myControllerWithService;

    private MyResponseService myResponseService;

    @Before
    public void setUp() throws Exception {
        myResponseService = mock(MyResponseService.class);
        myControllerWithService.withMyResponseService(myResponseService);
    }

    @ProviderState("response hello world for 'get /json'")
    public MyControllerWithService shouldResponseCorrectHelloWorldForGet() {
        when(myResponseService.<String>getResponse()).thenReturn(new ResponseEntity<String>("{ \"hello\": \"world\" }", HttpStatus.OK));
        return myControllerWithService;
    }

}
