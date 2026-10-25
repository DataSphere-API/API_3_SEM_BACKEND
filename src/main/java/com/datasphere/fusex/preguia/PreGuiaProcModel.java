package com.datasphere.fusex.preguia;

import com.datasphere.fusex.ocs.OcsModel;
import com.datasphere.fusex.procedimento.ProcedimentoModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "pre_guia_proc")
@Getter
@Setter
@NoArgsConstructor
public class PreGuiaProcModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pre_guia_id", nullable = false)
    private PreGuiaModel preGuiaModel;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "proc_id", nullable = false)
    private ProcedimentoModel procedimentoModel;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ocs_id", nullable = false)
    private OcsModel ocsModel;

    @Column(name = "qtd_autorizada", nullable = false)
    private Integer qtdAutorizada;
}