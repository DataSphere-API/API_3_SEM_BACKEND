package com.datasphere.fusex.espelhoItem;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "espelho_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EspelhoItemModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "espelho_id", nullable = false)
    private Integer espelhoId;

    @Column(name = "atendimento_id", nullable = false)
    private Long atendimentoId;

    @Column(name = "beneficiario_id", nullable = false, length = 20)
    private String beneficiarioId;

    @Column(name = "descricao", nullable = false, length = 150)
    private String descricao;

    @Column(name = "valor", nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;
}