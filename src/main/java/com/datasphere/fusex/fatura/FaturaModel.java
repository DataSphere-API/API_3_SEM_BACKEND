package com.datasphere.fusex.fatura;

import com.datasphere.fusex.espelho.EspelhoModel;
import com.datasphere.fusex.guiaProc.GuiaProcModel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "fatura")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FaturaModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "guia_proc_id", nullable = false)
    private GuiaProcModel guiaProcModel;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "esp_fat", nullable = false)
    private EspelhoModel espelhoModel;
}