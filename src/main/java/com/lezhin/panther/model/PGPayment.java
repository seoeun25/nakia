package com.lezhin.panther.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author seoeun
 * @since 2017.11.05
 */
public class PGPayment implements Serializable {

    @JsonIgnore
    protected String approvalId;

    public String getApprovalId() {
        return approvalId;
    }

    public void setApprovalId(String approvalId) {
        this.approvalId = approvalId;
    }

    public Map<String, Object> createReceipt() {
        return new HashMap<>();
    }
}
