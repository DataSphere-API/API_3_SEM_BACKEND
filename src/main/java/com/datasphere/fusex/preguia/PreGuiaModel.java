package com.datasphere.fusex.preguia;

import com.datasphere.fusex.beneficiario.BeneficiarioModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import java.time.LocalDateTime;

@Entity
@Table(name = "pre_guia")
@Getter
@Setter
@NoArgsConstructor
public class PreGuiaModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private StatusPreGuia status;

    @Column(name = "benf_id", nullable = false, length = 20)
    private String benfId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "benf_id", insertable = false, updatable = false)
    private BeneficiarioModel beneficiarioModel;

    @Column(name = "med_id", nullable = false)
    private Integer medId;

    @Column(name = "emissor_id")
    private Integer emissorId;

    @Column(name = "chefe_id")
    private Integer chefeId;

    @Column(name = "data_criacao", nullable = false)
    private LocalDate dataCriacao;

    @Column(name = "data_aprovacao_medico")
    private LocalDateTime dataAprovacaoMedico;

    @Column(name = "data_aprovacao_chefe")
    private LocalDateTime dataAprovacaoChefe;

    @Column(name = "data_reprovacao")
    private LocalDateTime dataReprovacao;

    @Column(name = "motivo_reprovacao")
    private String motivoReprovacao;

    @Column(name = "guia_id")
    private Long guiaId;
}