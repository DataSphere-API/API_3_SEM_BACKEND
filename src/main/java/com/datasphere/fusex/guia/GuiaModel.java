package com.datasphere.fusex.guia;

import com.datasphere.fusex.beneficiario.BeneficiarioModel;
import com.datasphere.fusex.medico.MedicoModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "guia")
public class GuiaModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "benf_id")
    private String benfId;

    @Column(name = "med_id")
    private Integer medId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "benf_id", insertable = false, updatable = false)
    private BeneficiarioModel beneficiarioModel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "med_id", insertable = false, updatable = false)
    private MedicoModel medicoModel;

    @Column(name = "valor_total", precision = 12, scale = 2)
    private BigDecimal valorTotal;

    private String pdf;
    private String data;
    private String status;

    public GuiaModel() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBenfId() {
        return benfId;
    }

    public Integer getMedId() {
        return medId;
    }

    public void setMedId(Integer medId) {
        this.medId = medId;
    }

    public void setBenfId(String benfId) {
        this.benfId = benfId;
    }

    public BeneficiarioModel getBeneficiarioModel() {
        return beneficiarioModel;
    }

    public MedicoModel getMedicoModel() {
        return medicoModel;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    public String getPdf() {
        return pdf;
    }

    public void setPdf(String pdf) {
        this.pdf = pdf;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
