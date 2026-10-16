package com.hieu.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class RecognizeResponse {

    private String status; // "success" | "error"
    private List<String> equations;
    private String filename;
    private String message;

    public RecognizeResponse() {
    }

    public static RecognizeResponse error(String message) {
        RecognizeResponse r = new RecognizeResponse();
        r.setStatus("error");
        r.setMessage(message);
        return r;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getEquations() {
        return equations;
    }

    public void setEquations(List<String> equations) {
        this.equations = equations;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
