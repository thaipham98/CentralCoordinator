package com.example.centralcoordinator.Controller;


import com.example.centralcoordinator.model.ForwardRequestRepr;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

//import javax.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.client.RestTemplate;

@RestController
@AllArgsConstructor
@RequestMapping("/**")
public class MainController {

    private PaxosHandler paxosHandler;

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<String> handleRequest(HttpServletRequest request, @RequestBody(required = false) String requestBody) {
        System.out.println("MainController: Received request: " + request.getMethod() + " " + request.getRequestURL() + " " + requestBody);
        ResponseEntity<String> responseEntity = paxosHandler.handleRequest(request, requestBody);
        return responseEntity;
    }

}
