package com.datasphere.fusex.atendimento.sessao;

import com.datasphere.fusex.guiaProc.GuiaProcModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "sessao_procedimento",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_sessao_guia_proc_numero",
                columnNames = {"guia_proc_id", "numero_sessao"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SessaoModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "guia_proc_id", nullable = false)
    private GuiaProcModel guiaProc;

    @Column(name = "numero_sessao", nullable = false)
    private Integer numeroSessao;

    @Column(name = "data_realizacao", nullable = false)
    private LocalDateTime dataRealizacao;

    @Column(name = "ocs_id", nullable = false, length = 14)
    private String ocsId;
}