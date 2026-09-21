package com.datasphere.fusex.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity

public class GuiaProc {
    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long guiaId;
    private Long contId;
    private String qr;
    private String status;

    public GuiaProc () {
    }

    public String getQr () {
        return qr;
    }

    public void setQr (String qr) {
        this.qr = qr;
    }

    public Long getGuiaId() {
        return guiaId;
    }

    public void setGuiaId (Long guiaId) {
        this.guiaId = guiaId;
    }

    public Long getContId () {
        return contId;
    }

    public void setContId (Long contId) {
        this.contId = contId;
    }

    public String getStatus () {
        return status;
    }

    public void setStatus (String status) {
        this.status = status;
    }
}
