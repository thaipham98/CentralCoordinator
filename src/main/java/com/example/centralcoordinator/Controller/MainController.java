package com.example.centralcoordinator.Controller;


import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

//import javax.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@AllArgsConstructor
@RequestMapping("/**")
public class MainController {

    private PaxosHandler paxosHandler;

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT})
    public ResponseEntity<Object> handleRequest(HttpServletRequest request) {
        return paxosHandler.handleRequest(request);


        //return ResponseEntity.ok("Hello World!");
    }

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<String> handleGetRequest(HttpServletRequest request){
        return processRequest(request, null);
    }

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<String> handlePostRequest(HttpServletRequest request, @RequestBody String requestBody) {
        return processRequest(request, requestBody);
    }

    @RequestMapping(method = RequestMethod.PUT) // value = "/**",
    public ResponseEntity<String> handlePutRequest(HttpServletRequest request, @RequestBody String requestBody){
        return processRequest(request, requestBody);
    }

    private ResponseEntity<String> processRequest(HttpServletRequest request, String requestBody){
        // Get information about the request
        String method = request.getMethod();
        String contentType = request.getContentType();
        String userAgent = request.getHeader("User-Agent");
        String url = request.getRequestURL().toString();
        String queryString = request.getQueryString();
        String pathInfo = request.getPathInfo();

        // Construct the response
        StringBuilder responseBuilder = new StringBuilder();
        responseBuilder.append("Method: " + method + "\n");
        responseBuilder.append("Content-Type: " + contentType + "\n");
        responseBuilder.append("User-Agent: " + userAgent + "\n");
        responseBuilder.append("URL: " + url + "\n");

        if (queryString != null) {
            responseBuilder.append("Query string: " + queryString + "\n");
        }
        if (pathInfo != null) {
            responseBuilder.append("Path info: " + pathInfo + "\n");
        }
        if (request != null){
            responseBuilder.append("Request body" + requestBody + "\n");
        }
        return ResponseEntity.ok(responseBuilder.toString());
    }




}
