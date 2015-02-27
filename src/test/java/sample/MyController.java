package sample;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class MyController {

    @RequestMapping(value = "/hello/plain", method = RequestMethod.GET)
    public ResponseEntity<String> helloPlain() {
        return new ResponseEntity<String>("world", HttpStatus.OK);
    }

    @RequestMapping(value = "/hello/json", method = RequestMethod.GET, consumes = "application/json", produces = "application/json;charset=UTF-8")
    public ResponseEntity<String> helloJson() {
        return new ResponseEntity<String>("{\"hello\":\"world\"}", HttpStatus.OK);
    }
}
