package com.datasphere.fusex.guiaProc;

import com.datasphere.fusex.contrato.ContratoModel;
import com.datasphere.fusex.guia.GuiaModel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

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
    private Long guiaId; // na prática é o id do atendimento (guia_proc.id)

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cont_id", nullable = false)
    private ContratoModel contratoModel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guia_id")
    private GuiaModel guiaModel;

    private String qr;

    private String status;

    @Column(name = "qtd_autorizada", nullable = false)
    private Integer qtdAutorizada;

    @Column(name = "qtd_realizada", nullable = false)
    private Integer qtdRealizada;

    @Column(name = "data_validade")
    private LocalDate dataValidade;
}