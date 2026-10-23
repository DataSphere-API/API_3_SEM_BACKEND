package com.datasphere.fusex.guia;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name="guia")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GuiaModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "benf_id", nullable = false)
    private String benfId;

    @Column(name = "emissor_id")
    private Integer emissorId;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private StatusGuia status;

    @Column(name = "data", nullable = false)
    private LocalDate dataSolicitacao;

    @Column(name = "data_emissao")
    private LocalDate dataEmissao;

    @Column(name = "motivo_reprovacao")
    private String motivoReprovacao;

    @Column(name = "data_aprovacao_chefe")
    private LocalDateTime dataAprovacaoChefe;

    @Column(name = "data_aprovacao_medico")
    private LocalDateTime dataAprovacaoMedico;

    @Column(name = "med_id")
    private Integer medId;

    @Column(name = "chefe_id")
    private Integer chefeId;

}