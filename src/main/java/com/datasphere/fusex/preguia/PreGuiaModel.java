package com.datasphere.fusex.preguia;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @Column(name = "chefe_id")
    private Long chefeId;

    @Column(name = "data_decisao")
    private LocalDateTime dataDecisao;

    @Column(name = "motivo_reprovacao", columnDefinition = "TEXT")
    private String motivoReprovacao;
}