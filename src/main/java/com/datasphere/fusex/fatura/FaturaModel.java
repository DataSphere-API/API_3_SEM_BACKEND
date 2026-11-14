package com.datasphere.fusex.fatura;

import com.datasphere.fusex.espelho.EspelhoModel;
import com.datasphere.fusex.guiaProc.GuiaProcModel;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "Fatura")
@Data
public class FaturaModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ocs_id", length = 14)
    private String ocsId;

    @Column(name = "competencia", length = 7)
    private String competencia;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private StatusFatura status;

    @Column(name = "data_encaminhamento")
    private LocalDateTime dataEncaminhamento;

    @Column(name = "data_geracao")
    private LocalDateTime dataGeracao;

    @Column(name = "fatura_origem_id")
    private Long faturaOrigemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "esp_fat", nullable = true)
    private EspelhoModel espelhoModel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guia_proc_id", nullable = true)
    private GuiaProcModel guiaProcModel;
}