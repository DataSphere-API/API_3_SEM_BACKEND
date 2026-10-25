package com.datasphere.fusex.guia;

import com.datasphere.fusex.beneficiario.BeneficiarioModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "guia")
@Getter
@Setter
@NoArgsConstructor
public class GuiaModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "benf_id")
    private String benfId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "benf_id", insertable = false, updatable = false)
    private BeneficiarioModel beneficiarioModel;

    @Column(name = "med_id")
    private Integer medId;

    @Column(name = "emissor_id")
    private Integer emissorId;

    @Column(name = "chefe_id")
    private Integer chefeId;

    @Column(name = "data_emissao")
    private LocalDate dataEmissao;

    private String status;
}