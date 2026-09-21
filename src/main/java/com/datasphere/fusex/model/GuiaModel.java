package com.datasphere.fusex.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class GuiaModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id;
    private Long benfId;
    private String pdf;
    private String data;
    private String status;

    public GuiaModel() {
        }

        public Long getId () {
        return id;
    }

    public void setId (Long id) {
        this.id = id;
    }

    public Long getBenfId () {
        return benfId;
    }

    public void setBenfId (Long benfId) {
        this.benfId = benfId;
    }

    public String getPdf () {
        return pdf;
    }

    public void setPdf (String pdf) {
        this.pdf = pdf;
    }

    public String getData () {
        return data;
    }

    public void setData (String data) {
        this.data = data;
    }

    public String getStatus () {
        return  status;
    }

    public void setStatus (String status) {
        this.status = status;
    }
}