package com.datasphere.fusex.espelho;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "espelho")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EspelhoModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "atendimento_id", nullable = false, unique = true)
    private Long atendimentoId;

    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    @Column(name = "data_fim", nullable = false)
    private LocalDate dataFim;

    @Column(name = "ocs_id", length = 14)
    private String ocsId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StatusEspelho status;

    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Transient
    private String ocsNome;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Transient
    private Integer quantidadeItens;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Transient
    private BigDecimal valorTotal;

    @PreUpdate
    private void marcarAtualizacao() {
        dataAtualizacao = LocalDateTime.now();
    }
}