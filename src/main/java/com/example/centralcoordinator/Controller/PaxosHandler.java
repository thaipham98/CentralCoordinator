package com.example.centralcoordinator.Controller;

import Configuration.ApplicationProperties;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Service
public class PaxosHandler {
    private List<Integer> nodePorts;
    private List<String> nodeHostnames;
    private int port;
    private String hostname;
    private Long currentProposal;
    private HttpServletRequest currentValue;
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

    private ResponseEntity<Object> consensusReached() {
        ResponseEntity<Object> result = null;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        for (int i = 0; i < nodePorts.size(); i++) {
            //send request to each node via HTTP
            String url = "http://" + nodeHostnames.get(i) + ":" + nodePorts.get(i);

            RestTemplate restTemplate = new RestTemplate();
            HttpEntity<Object> requestEntity = null;
            try {
                requestEntity = new HttpEntity<>(currentValue.getInputStream().readAllBytes(), headers);
                result = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Object.class);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }

        currentValue = null;
        numTrials = 0;

        return result;
    }

    private boolean sendPropose(Long currentProposal, HttpServletRequest currentValue) {
        int numAccepted = 0;

        for (int i = 0; i < nodePorts.size(); i++) {
            //create a get POST request with endpoint /prepare and paramater proposalId = currentProposal
            //send request to each node via HTTP
            RestTemplate restTemplate = new RestTemplate();

            String url = "http://" + nodeHostnames.get(i) + ":" + nodePorts.get(i) + "/accept";
            //create body with proposalId and value
//            body = (1,2)
//            request = wrap(body)
//            response = send(request + url)

             //= restTemplate.getForObject(url, ResponseEntity.class);


//            if (accepted != null && accepted.getAccepted()) {
//                numAccepted++;
//            }
            //System.out.println("Response Body: " + response);
        }

        if (numAccepted < nodePorts.size() / 2) {
            return false;
        }

        return true;
    }

    public boolean sendPrepare(Long currentProposal) {
        int numPrepared = 0;
        HttpServletRequest valueToSend = null;

        for (int i = 0; i < nodePorts.size(); i++) {

            //send request to each node via HTTP
            RestTemplate restTemplate = new RestTemplate();

            String url = "http://" + nodeHostnames.get(i) + ":" + nodePorts.get(i) + "/prepare?proposalId=" + currentProposal;
            System.out.println(url);
            ResponseEntity<Object> response = passRequest(url, null);
            // extracting response value
            System.out.println(response.getBody());
            // update numPrepared based on response
            if (response.getStatusCode() == HttpStatus.OK) {
                numPrepared++;
            }


        }

        if (numPrepared < nodePorts.size() / 2) {
            return false;
        }

        if (valueToSend != null) {
            currentValue = valueToSend;
        }

        return true;
    }

    // paxosHandler.java

    public ResponseEntity<Object> passRequest(String url, String requestBody){
        // url must contain the endpoint with currentProposal, or whatever to hit
        //send request to each node via HTTP
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> requestEntity;
        if (requestBody != null) {
            requestEntity = new HttpEntity<>(requestBody, headers);
        } else {
            requestEntity = new HttpEntity<>("", headers);
        }

//        String url = "http://localhost:1001/prepare?proposalId=" + currentProposal;
        ResponseEntity<Object> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity,  Object.class);
        return response;
    }

}
