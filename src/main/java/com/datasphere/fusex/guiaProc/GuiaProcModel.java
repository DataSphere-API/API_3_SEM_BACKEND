package com.datasphere.fusex.guiaProc;

import com.datasphere.fusex.contrato.ContratoModel;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "guia_proc")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GuiaProcModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long guiaId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cont_id", nullable = false)
    private ContratoModel contratoModel;

    private String qr;

    private String status;
}