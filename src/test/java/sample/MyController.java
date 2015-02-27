package sample;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping(consumes = "application/json", produces = "application/json;charset=UTF-8")
public class MyController {

    @RequestMapping(value = "/hello", method = RequestMethod.GET)
    public ResponseEntity<String> hello() {
        return new ResponseEntity<String>("{\"hello\":\"world\"}", HttpStatus.OK);
    }

}
