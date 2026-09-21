package com.datasphere.fusex.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "contrato")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ContratoModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "proc_id", nullable = false)
    private ProcedimentoModel procedimentoModel;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ocs_id", nullable = false)
    private OcsModel ocsModel;

    @Column(name = "valor", nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;
}