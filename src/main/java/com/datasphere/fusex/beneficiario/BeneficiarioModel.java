package com.datasphere.fusex.beneficiario;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "beneficiario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BeneficiarioModel {

    @Id
    @Column(name = "preccp", length = 20, nullable = false)
    private String precCP;

    @Column(name = "dependente", nullable = false)
    private boolean dependente;

    @Column(name = "grupo", length = 50)
    private String grupo;

    @Column(name = "data_nasc")
    private LocalDate dataNasc;

    @Column(name = "nome", length = 150, nullable = false)
    private String nome;

    @Column(name = "ug", length = 100)
    private String ug;

    @Column(name = "cotista", nullable = false)
    private boolean cotista;

    @Column(name = "usuario_id")
    private Integer usuarioId;
}