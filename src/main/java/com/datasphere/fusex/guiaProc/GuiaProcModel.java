package com.datasphere.fusex.guiaProc;

import com.datasphere.fusex.contrato.ContratoModel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "guia_proc")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GuiaProcModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "guia_id", nullable = false)
    private Integer guiaId;

    @Column(name = "status", nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private StatusProcedimento status;

    @Column(name = "qtd_autorizada", nullable = false)
    private Integer qtdAutorizada;

    @Column(name = "qtd_realizada", nullable = false)
    private Integer qtdRealizada;

    @Column(name = "data_validacao")
    private LocalDateTime dataValidacao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cont_id", nullable = false)
    private ContratoModel contratoModel;
}