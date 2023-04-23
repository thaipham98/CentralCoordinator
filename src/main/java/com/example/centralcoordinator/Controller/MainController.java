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

//    @RequestMapping("/**")
//    public ResponseEntity<String> handleGetRequest(HttpServletRequest request){
//        // TODO: call paxosHandler.handleRequest() to send a propose message to the paxos group
//        boolean isPrepared = paxosHandler.sendPrepare(1L);
//        boolean isAccepted = paxosHandler.sendPropose(1L, request, null); //  get request doesnt have a body
//        System.out.println("isPrepared: " + isPrepared + ", isAccepted: " + isAccepted);
//        ResponseEntity<String> responseEntity = paxosHandler.consensusReached(request, null); // get request has a null body
//        return responseEntity;
//    }
//    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT})
//    public ResponseEntity<Object> handleRequest(HttpServletRequest request) {
//        return paxosHandler.handleRequest(request);
//       //return processRequest(request, null);
//    }

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<String> handleGetRequest(HttpServletRequest request){
        // TODO: call paxosHandler.handleRequest() to send a propose message to the paxos group
//        boolean isPrepared = paxosHandler.sendPrepare(1L);
//        boolean isAccepted = paxosHandler.sendPropose(1L, request, null); //  get request doesnt have a body
//        System.out.println("isPrepared: " + isPrepared + ", isAccepted: " + isAccepted);
        ResponseEntity<String> responseEntity = paxosHandler.consensusReached(request, null); // get request has a null body
        return responseEntity;
    }

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<String> handlePostRequest(HttpServletRequest request, @RequestBody String requestBody) {
        ResponseEntity<String> responseEntity = paxosHandler.consensusReached(request, requestBody); // get request has a null body
        return responseEntity;
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
            responseBuilder.append("Request body: " + requestBody + "\n");
        }
        System.out.println("MainController: processRequest: responseBuilder.toString(): ");
        System.out.println(responseBuilder.toString());
        return ResponseEntity.ok(responseBuilder.toString());
    }




}
