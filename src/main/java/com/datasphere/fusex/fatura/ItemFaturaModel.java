package com.datasphere.fusex.fatura;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "item_fatura")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ItemFaturaModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "fatura_id", nullable = false)
    private Long faturaId;

    @Column(name = "espelho_item_id", nullable = false)
    private Integer espelhoItemId;

    @Column(name = "valor_apresentado", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorApresentado;

    @Column(name = "valor_contratado", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorContratado;

    @Column(name = "divergencia_valor", nullable = false, precision = 10, scale = 2)
    private BigDecimal divergenciaValor;
}