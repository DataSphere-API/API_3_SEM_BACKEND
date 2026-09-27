package com.datasphere.fusex.guiaProc;

import com.datasphere.fusex.contrato.ContratoModel;
import jakarta.persistence.*;
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
    @Column(name = "id")
    private Long guiaId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cont_id", nullable = false)
    private ContratoModel contratoModel;

    private String qr;

    private String status;
}