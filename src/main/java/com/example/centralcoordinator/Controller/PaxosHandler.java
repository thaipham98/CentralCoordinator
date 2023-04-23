package com.example.centralcoordinator.Controller;

import Configuration.ApplicationProperties;
import com.example.centralcoordinator.model.ForwardRequestRepr;
import com.example.centralcoordinator.model.PaxosResponse;
import com.example.centralcoordinator.model.Promise;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
public class PaxosHandler {
    private List<Integer> nodePorts;
    private List<String> nodeHostnames;
    private int port;
    private String hostname;
    private Long currentProposal;
    private HttpServletRequest currentValue; // forwardRequestRepr ?
    private int numTrials;

    public PaxosHandler(ApplicationProperties props) {
        this.nodePorts = props.getNodePorts();
        this.nodeHostnames = props.getNodeHostnames();
        this.port = 8080;
        this.hostname = "localhost";
        this.currentProposal = 0L;
        this.currentValue = null;
        this.numTrials = 0;
    }

    public ResponseEntity<Object> handleRequest(HttpServletRequest request) {
        System.out.println(nodePorts);

        if (numTrials > 3) {
            numTrials = 0;
            return ResponseEntity.status(500).body("Server Error");
        }

        //TODO set to timestamp
        String currentProposalString = new SimpleDateFormat("MMddHHmmssSSS").format(new Date());
        currentProposal = Long.parseLong(currentProposalString);


        boolean isPrepared = sendPrepare(currentProposal);
        return null;

//        if (!isPrepared) {
//            numTrials++;
//            return handleRequest(request);
//        }
//
//
//        if (currentValue == null) {
//            currentValue = request;
//        }
//
//        //paxos accept phase
//        boolean isAccepted = sendPropose(currentProposal, currentValue);
//
//        if (!isAccepted) {
//            numTrials++;
//            return handleRequest(request);
//        }
//
//
//        //consensus is reached
//        return consensusReached();
    }


    public ResponseEntity<String> consensusReached(HttpServletRequest currentValue, String body) {
        // print the ccurrentValue
        System.out.println("==== From paxos handler: consensusReached currentValue:");
        HttpRequestToString(currentValue, null );
        // route request to correct server controller, or server request
        ResponseEntity<String> result = null;
        HttpMethod method = HttpMethod.valueOf(currentValue.getMethod());
        String path = currentValue.getRequestURI();
        String queryString = currentValue.getQueryString();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        for (int i = 0; i < nodePorts.size(); i++) {
            //send request to each node via HTTP
            String base_url = "http://" + nodeHostnames.get(i) + ":" + nodePorts.get(i);

            String forwardUrl = base_url + path + "?" + queryString;

            RestTemplate restTemplate = new RestTemplate();
            HttpEntity<Object> requestEntity = null;
            // TODO: change the body to custom object or String
            requestEntity = new HttpEntity<>(body, headers); // body, headers
            ResponseEntity<String> serverResponse = restTemplate.exchange(forwardUrl, method, requestEntity, String.class);
            // print the result
            System.out.println("==== From paxos handler: From server " + i + "Response:\n" + serverResponse);
            if (serverResponse.getStatusCode() == HttpStatus.OK) {
                result = serverResponse;
            }
        }

        currentValue = null;
        numTrials = 0;
        return result; //ResponseEntity.status(200).body("Consensus Reached");
    }

    public boolean sendPropose(Long currentProposal, HttpServletRequest currentValue, String forwardRequestBody) {
        System.out.println("sendPropose with HTTP request:\n" + HttpRequestToString(currentValue, null));
        int numAccepted = 0;
        ForwardRequestRepr valueToSend = new ForwardRequestRepr(currentValue, forwardRequestBody);
        System.out.println("sendPropose with ForwardRequestRepr:\n" + valueToSend);
        String acceptUrl;

        // send to accept endpoint of Paxos Controller on server. To make server replica aware of this request.
        for (int i = 0; i < nodePorts.size(); i++) {
            acceptUrl = "http://" + nodeHostnames.get(i) + ":" + nodePorts.get(i) + "/accept?proposalId=" + currentProposal;
            System.out.println("acceptUrl:" + acceptUrl);
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            //create request body
            HttpEntity<ForwardRequestRepr> requestEntity = new HttpEntity<>(valueToSend, headers); // body and header
            ResponseEntity<String> response = restTemplate.exchange(acceptUrl, HttpMethod.POST, requestEntity, String.class);

            // extracting response value
            PaxosResponse paxosResponse = this.parsePaxosResponse(response);
            if (paxosResponse != null) {
                // extract promise from response
                Promise promise = paxosResponse.getData();
                if (promise != null && promise.isAccepted()) {
                    numAccepted++;
                }
            }
        }

        if (numAccepted <= nodePorts.size() / 2) {
            return false;
        }
        System.out.println("==== From paxos handler: sendPropose numAccepted:" + numAccepted);

        return true;
    }

    public boolean sendPrepare(Long currentProposal) {
        int numPrepared = 0;
        HttpServletRequest valueToSend = null;
        String prepareUrl;

        for (int i = 0; i < nodePorts.size(); i++) {
            prepareUrl = "http://" + nodeHostnames.get(i) + ":" + nodePorts.get(i) + "/prepare?proposalId=" + currentProposal;
            System.out.println("prepareUrl:" + prepareUrl);
            // send to prepare endpoint of Paxos Controller on server
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            //create request body
            HttpEntity<HttpServletRequest> requestEntity = new HttpEntity<>(null, headers); // body and header
            ResponseEntity<String> response = restTemplate.exchange(prepareUrl, HttpMethod.POST, requestEntity, String.class);

            // extracting response value
            PaxosResponse paxosResponse = this.parsePaxosResponse(response);
            if (paxosResponse != null) {
                // extract promise from response
                Promise promise = paxosResponse.getData();
                if (promise != null && promise.isPrepared()) {
                    numPrepared++;
                }
                // TODO: re-write this to map received json to ForwardRequestRepr
//                if (promise != null && promise.getAcceptedValueHttpServletRequest() != null) {
//                    valueToSend = promise.getAcceptedValueHttpServletRequest();
//                }
            }
        }

        System.out.println("===From sendPrepare: proposalId = " + currentProposal + " numPrepared: " + numPrepared + "===");

        if (numPrepared <= nodePorts.size() / 2) {
            return false;
        }

        if (valueToSend != null) {
            currentValue = valueToSend;
        }

        return true;
    }
    // =========== Helper functions to pass request to other nodes and parse Response ===========

    public ResponseEntity<String> passRequest(String urlToSend, HttpServletRequest request){
        // url must contain the endpoint with currentProposal, or whatever to hit
        //send request to each node via HTTP
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        //create request body
        HttpEntity<HttpServletRequest> requestEntity = new HttpEntity<>(request, headers);
        ResponseEntity<String> response = restTemplate.exchange(urlToSend, HttpMethod.POST, requestEntity, String.class);

        return response;
    }

    private PaxosResponse parsePaxosResponse(ResponseEntity<String> response){
        // extract response value
        // update numPrepared based on response
        // if response is not OK, return false
        // if response is OK, return true
        try{
            if (response.getStatusCode() == HttpStatus.OK) {
                //parse response body
                ObjectMapper mapper = new ObjectMapper();
                // TODO: parse nested object properly:
                PaxosResponse responseObject = mapper.readValue(response.getBody(), PaxosResponse.class);
                String message = responseObject.getMessage();
                int status = responseObject.getStatus();
                Promise data = responseObject.getData();
                // print response objects
                System.out.println("Message: " + message);
                System.out.println("Status: " + status);
                System.out.println("Data: " + data);

                //do something with the parsed values
                return responseObject;
            } else {
                //handle error response
                // TODO: change this by sth else than null
                System.out.println("Error response: " + response.getStatusCode());
                return null;
            }
        } catch (JacksonException e) {
            //handle error response
            System.out.println("Error parsing response: " + e.getMessage());
            return null;
        }

    }

    private String HttpRequestToString(HttpServletRequest request, String requestBody) {
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
        return responseBuilder.toString();
    }

}
