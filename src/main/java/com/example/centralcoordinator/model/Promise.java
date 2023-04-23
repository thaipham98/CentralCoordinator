package com.example.centralcoordinator.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;

// Copied from Server Promise class
public class Promise {
    @JsonProperty("acceptedProposal")
    private Long acceptedProposal;
    @JsonProperty("acceptedValue")
    private ForwardRequestRepr acceptedValue; //  request from client
    @JsonProperty("accepted")
    private boolean accepted;
    @JsonProperty("prepared")
    private boolean prepared;

    public Promise() {
    }

    public Promise(Long acceptedProposal, ForwardRequestRepr acceptedValue, boolean accepted, boolean prepared) {
        this.acceptedProposal = acceptedProposal;
        this.acceptedValue = acceptedValue;
        this.accepted = accepted;
        this.prepared = prepared;
    }

    public Promise(ForwardRequestRepr acceptedValue, boolean accepted, boolean prepared) {
        this.acceptedValue = acceptedValue;
        this.accepted = accepted;
        this.prepared = prepared;
    }

    public Promise(Long acceptedProposal, boolean accepted, boolean prepared) {
        this.acceptedProposal = acceptedProposal;
        this.accepted = accepted;
        this.prepared = prepared;
    }

    public Long getAcceptedProposal() {
        return acceptedProposal;
    }

    public ForwardRequestRepr getAcceptedValue() {
        return acceptedValue;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public boolean isPrepared() {
        return prepared;
    }

    public void setAcceptedProposal(Long acceptedProposal) {
        this.acceptedProposal = acceptedProposal;
    }

    public void setAcceptedValue(ForwardRequestRepr acceptedValue) {
        this.acceptedValue = acceptedValue;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }

    public void setPrepared(boolean prepared) {
        this.prepared = prepared;
    }

    @Override
    public String toString() {
//        if (this.acceptedValue == null){
//            return "Promise{" +
//                    "acceptedProposal=" + acceptedProposal +
//                    ", acceptedValue=" + null +
//                    ", accepted=" + accepted +
//                    ", prepared=" + prepared +
//                    '}';
//        }
        return "Promise{" +
                "acceptedProposal=" + acceptedProposal +
                ", acceptedValue=" + acceptedValue +
                ", accepted=" + accepted +
                ", prepared=" + prepared +
                '}';
    }
}
