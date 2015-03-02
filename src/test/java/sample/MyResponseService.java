package sample;


import org.springframework.http.ResponseEntity;

public interface MyResponseService {

    <T> ResponseEntity<T> getResponse();

}
