package sample;

import org.springframework.http.ResponseEntity;

public interface MyService {

    <T> ResponseEntity<T> getResponse();

    <T> ResponseEntity<T> getResponseForCookies(String[] cookies);

}
