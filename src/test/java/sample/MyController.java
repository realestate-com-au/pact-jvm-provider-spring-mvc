package sample;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

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

    @RequestMapping(value = "/hello/querystring", method = RequestMethod.GET)
    public ResponseEntity<String> helloQueryString(@RequestParam(value = "name") String name, @RequestParam(value = "phones") String[] phones) {
        String[] myPhones = {"111111", "222222"};
        if (StringUtils.equals(name, "pact") && ArrayUtils.isEquals(phones, myPhones)) {
            return new ResponseEntity<String>("good", HttpStatus.OK);
        } else {
            return new ResponseEntity<String>("unknown", HttpStatus.OK);
        }
    }

}
