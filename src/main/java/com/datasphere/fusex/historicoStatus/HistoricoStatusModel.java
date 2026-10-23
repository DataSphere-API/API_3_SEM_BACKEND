package com.datasphere.fusex.historicoStatus;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "historico_status", indexes = {
        @Index(name = "idx_hist_entidade", columnList = "entidade_tipo, entidade_id"),
        @Index(name = "idx_hist_data", columnList = "data")
})
public class HistoricoStatusModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "entidade_tipo", nullable = false, length = 30)
    private EntidadeTipo entidadeTipo;

    @Column(name = "entidade_id", nullable = false)
    private Long entidadeId;

    @Column(name = "status_anterior", length = 50)
    private String statusAnterior;

    @Column(name = "status_novo", nullable = false, length = 50)
    private String statusNovo;

    @Column(name = "data", nullable = false)
    private LocalDateTime data;

    @Column(name = "origem", nullable = false, length = 50)
    private String origem;

    public HistoricoStatusModel(EntidadeTipo tipo, Long entidadeId, String statusAnterior, String statusNovo, String origem) {
    }
}