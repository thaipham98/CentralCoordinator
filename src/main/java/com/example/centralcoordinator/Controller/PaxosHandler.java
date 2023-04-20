package com.example.centralcoordinator.Controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;

public class PaxosHandler {

    private String hostname;
    private List<Integer> nodePorts;
    private List<String> nodeHostnames;
    private int port;
    private Long currentProposal;
    private HttpServletRequest currentValue;
    private int numTrials;

    public ResponseEntity<Object> handleRequest(HttpServletRequest request) {

        if (numTrials > 3) {
            numTrials = 0;
            return ResponseEntity.status(500).body("Server Error");
        }

        //TODO set to timestamp
        currentProposal = System.currentTimeMillis();


        boolean isPrepared = sendPrepare(currentProposal);

        if (!isPrepared) {
            numTrials++;
            return handleRequest(request);
        }


        if (currentValue == null) {
            currentValue = request;
        }

        //paxos accept phase
        boolean isAccepted = sendPropose(currentProposal, currentValue);

        if (!isAccepted) {
            numTrials++;
            return handleRequest(request);
        }


        //consensus is reached
        return consensusReached();
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
//                    response = send(request + url)

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

    private boolean sendPrepare(Long currentProposal) {
        int numPrepared = 0;
        HttpServletRequest valueToSend = null;

        for (int i = 0; i < nodePorts.size(); i++) {

            //send request to each node via HTTP
            RestTemplate restTemplate = new RestTemplate();

            String url = "http://" + nodeHostnames.get(i) + ":" + nodePorts.get(i) + "/accept?proposalId=" + currentProposal;
            ResponseEntity<Object> response = restTemplate.getForObject(url, ResponseEntity.class);

            //TODO

//            if (promise != null && promise.getPrepared()) {
//                numPrepared++;
//            }
//
//            if (promise != null && promise.getAccepted()) {
//                valueToSend = promise.getAcceptedValue();
//            }

        }

        if (numPrepared < nodePorts.size() / 2) {
            return false;
        }

        if (valueToSend != null) {
            currentValue = valueToSend;
        }

        return true;
    }
}
