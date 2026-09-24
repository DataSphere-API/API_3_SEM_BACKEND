package com.datasphere.fusex.historicoStatus;

import jakarta.persistence.*;
import java.time.LocalDateTime;

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

    public HistoricoStatusModel() {}

    public HistoricoStatusModel(EntidadeTipo entidadeTipo, Long entidadeId,
                                String statusAnterior, String statusNovo,
                                String origem) {
        this.entidadeTipo = entidadeTipo;
        this.entidadeId = entidadeId;
        this.statusAnterior = statusAnterior;
        this.statusNovo = statusNovo;
        this.origem = origem;
        this.data = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public EntidadeTipo getEntidadeTipo() { return entidadeTipo; }
    public Long getEntidadeId() { return entidadeId; }
    public String getStatusAnterior() { return statusAnterior; }
    public String getStatusNovo() { return statusNovo; }
    public LocalDateTime getData() { return data; }
    public String getOrigem() { return origem; }
}