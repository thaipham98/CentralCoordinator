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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
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
    private ForwardRequestRepr mockCurrentValue; // current request to send with body for paxos
    private int numTrials;

    public PaxosHandler(ApplicationProperties props) {
        this.nodePorts = props.getNodePorts();
        this.nodeHostnames = props.getNodeHostnames();
        this.port = 8080;
        this.hostname = "localhost";
        this.currentProposal = 0L;
        this.mockCurrentValue = null;
        this.numTrials = 0;
    }

    public ResponseEntity<String> handleRequest(HttpServletRequest request, String requestBody) {
        System.out.println(nodePorts);

        try {
            if (numTrials > 3) {
                numTrials = 0;
                return ResponseEntity.status(500).body("Server Error");
            }

            //TODO set to timestamp
            String currentProposalString = new SimpleDateFormat("MMddHHmmssSSS").format(new Date());
            currentProposal = Long.parseLong(currentProposalString);


            boolean isPrepared = sendPrepare(currentProposal);

            if (!isPrepared) {
//            numTrials++;
//            return handleRequest(request, requestBody);
                return ResponseEntity.status(500).body("The majority of server replicas are not prepared");
            }

            if (this.mockCurrentValue == null) {
                this.mockCurrentValue = new ForwardRequestRepr(request, requestBody);
            }

            System.out.println("===== From paxos handler: Majority of replicas are prepared. Current proposal: " + currentProposal + "Proceed to accept phase");

            //paxos accept phase
            boolean isAccepted = sendPropose(currentProposal, this.mockCurrentValue);

            if (!isAccepted) {
//            numTrials++;
//            return handleRequest(request, requestBody);
                return ResponseEntity.status(500).body("The majority of server replicas are not accepted");
            }

            System.out.println("===== From paxos handler: Majority of replicas are accepted. Current proposal: " + currentProposal + "Value acccepted:" + this.mockCurrentValue + "Proceed to decide phase");

            //consensus is reached
            int numDecide = this.sendDecide();
            System.out.println("===== From paxos handler: Consensus reached. numDecide = " + numDecide);
            ResponseEntity<String> response = consensusReached(this.mockCurrentValue);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Server Error " + e.getMessage());
        }
    }


    public ResponseEntity<String> consensusReached(ForwardRequestRepr mockCurrentValue) {
        // print the ccurrentValue
        System.out.println("==== From paxos handler: consensusReached mockCurrentValue:" + mockCurrentValue);
        // route request to correct server controller, or server request
        ResponseEntity<String> result = null;
        HttpMethod method = HttpMethod.valueOf(mockCurrentValue.getMethod());
        String path = mockCurrentValue.getRequestURI();
        String queryString = mockCurrentValue.getQueryString();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        for (int i = 0; i < nodePorts.size(); i++) {
            //send request to each node via HTTP
            String base_url = "http://" + nodeHostnames.get(i) + ":" + nodePorts.get(i);

            String forwardUrl = base_url + path + "?" + queryString;

            RestTemplate restTemplate = new RestTemplate();
            HttpEntity<Object> requestEntity = new HttpEntity<>(mockCurrentValue.getBody(), headers); // body, headers
            try{
                ResponseEntity<String> serverResponse = restTemplate.exchange(forwardUrl, method, requestEntity, String.class);
                // print the result
                System.out.println("==== From paxos handler: From server " + i + "Response:\n" + serverResponse);
                result = serverResponse;
            } catch (ResourceAccessException e){
                // server not available, just print message and skip
                System.out.println("Error sending ACTUAL request to server " + this.nodeHostnames.get(i) + ":" + this.nodePorts.get(i));
                System.out.println(e.getMessage());
            } catch (HttpClientErrorException e){
                // malformed request, return error. All server will response the same anyway
                result = ResponseEntity
                        .status(e.getStatusCode())
                        .headers(e.getResponseHeaders())
                        .body(e.getResponseBodyAsString());
            } catch (Exception e){
                // any other exception, return 500 error.
                System.out.println("Error sending ACTUAL request to server: " + this.nodeHostnames.get(i) + ":" + this.nodePorts.get(i) + "\n" + e.getMessage());
                System.out.println(e.getMessage());
                result = ResponseEntity.status(500).body(e.getMessage());
            }
        }

        this.mockCurrentValue = null;
        this.numTrials = 0;
        return result; //ResponseEntity.status(200).body("Consensus Reached");
    }

    public int sendDecide(){
        // assume once accept succeeds, decide succeeds
        // send decide to all nodes
        String decideUrl;
        int numDecided = 0;
        for (int i = 0; i < nodePorts.size(); i++){
            decideUrl = "http://" + nodeHostnames.get(i) + ":" + nodePorts.get(i) + "/decide";
            System.out.println("decideUrl:" + decideUrl);
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            //create request body
            HttpEntity<ForwardRequestRepr> requestEntity = new HttpEntity<>(null, headers); // body and header
            try {
                ResponseEntity<String> response = restTemplate.exchange(decideUrl, HttpMethod.POST, requestEntity, String.class);
                if (response.getStatusCode().equals(HttpStatus.OK)){
                    numDecided++;
                }
            } catch (Exception e) {
                System.out.println("Error sending decide request to server: " + this.nodeHostnames.get(i) + ":" + this.nodePorts.get(i) + "\n" + e.getMessage());
                System.out.println(e.getMessage());
            }
        }
        return numDecided;
    }

    public boolean sendPropose(Long currentProposal, ForwardRequestRepr mockCurrentValue) {
        System.out.println("sendPropose with HTTP request:\n" + mockCurrentValue);
        int numAccepted = 0;
        ForwardRequestRepr valueToSend = mockCurrentValue;
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
            try{
                ResponseEntity<String> response = restTemplate.exchange(acceptUrl, HttpMethod.POST, requestEntity, String.class);
                System.out.println("Received Paxos Response from server:");
                // extracting response value
                PaxosResponse paxosResponse = this.parsePaxosResponse(response);
                if (paxosResponse != null) {
                    // extract promise from response
                    Promise promise = paxosResponse.getData();
                    if (promise != null && promise.isAccepted()) {
                        numAccepted++;
                    }
                }
            } catch (Exception e) {
                System.out.println("Error sending propose request to server: " + this.nodeHostnames.get(i) + ":" + this.nodePorts.get(i) + "\n" + e.getMessage());
                System.out.println(e.getMessage());
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
        ForwardRequestRepr valueToSend = null;
        String prepareUrl;

        for (int i = 0; i < nodePorts.size(); i++) {
            prepareUrl = "http://" + nodeHostnames.get(i) + ":" + nodePorts.get(i) + "/prepare?proposalId=" + currentProposal;
            System.out.println("prepareUrl:" + prepareUrl);
            // send to prepare endpoint of Paxos Controller on server
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            //create request body
            HttpEntity<ForwardRequestRepr> requestEntity = new HttpEntity<>(null, headers); // in prepare phase, we dont send value

            try{
                ResponseEntity<String> response = restTemplate.exchange(prepareUrl, HttpMethod.POST, requestEntity, String.class);
                // extracting response value
                PaxosResponse paxosResponse = this.parsePaxosResponse(response);
                if (paxosResponse != null) {
                    // extract promise from response
                    Promise promise = paxosResponse.getData();
                    if (promise != null && promise.isPrepared()) {
                        numPrepared++;
                    }
                    if (promise != null && promise.getAcceptedValue() != null) {
                        valueToSend = promise.getAcceptedValue();
                    }
                }

            } catch (Exception e){
                // eg. server not available
                System.out.println("Exception in sendPrepare to server: " + this.nodeHostnames.get(i) + ":" + this.nodePorts.get(i));
                System.out.println(e.getMessage());
            }

        }

        System.out.println("===From sendPrepare: proposalId = " + currentProposal + " numPrepared: " + numPrepared + "===");

        if (numPrepared <= nodePorts.size() / 2) {
            return false;
        }

        if (valueToSend != null) {
            mockCurrentValue = valueToSend;
        }

        return true;
    }
    // =========== Helper functions to pass request to other nodes and parse Response ===========

    private PaxosResponse parsePaxosResponse(ResponseEntity<String> response){
        // extract response value
        // print response received from paxos controller
        System.out.println("Response received from paxos controller:" + response.getBody());
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
                System.out.println("Parsed response body received from paxos controller:");
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
}
