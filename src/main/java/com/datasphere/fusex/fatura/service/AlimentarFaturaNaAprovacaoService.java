package com.datasphere.fusex.fatura.service;

import com.datasphere.fusex.espelho.EspelhoModel;
import com.datasphere.fusex.espelho.EspelhoRepository;
import com.datasphere.fusex.espelho.StatusEspelho;
import com.datasphere.fusex.espelhoitem.EspelhoItemModel;
import com.datasphere.fusex.espelhoitem.EspelhoItemRepository;
import com.datasphere.fusex.fatura.FaturaModel;
import com.datasphere.fusex.fatura.FaturaRepository;
import com.datasphere.fusex.fatura.StatusFatura;
import com.datasphere.fusex.guiaproc.GuiaProcModel;
import com.datasphere.fusex.guiaproc.GuiaProcRepository;
import com.datasphere.fusex.historicostatus.EntidadeTipo;
import com.datasphere.fusex.historicostatus.service.RegistrarMudancaStatusService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class AlimentarFaturaNaAprovacaoService {

    private static final String ORIGEM = "inclusao-na-fatura";

    private final FaturaRepository faturaRepository;
    private final EspelhoRepository espelhoRepository;
    private final EspelhoItemRepository espelhoItemRepository;
    private final GuiaProcRepository guiaProcRepository;
    private final MontarItensFaturaService montarItensFaturaService;
    private final AtualizarStatusGuiaProcParaFaturadaService atualizarStatusGuiaProcParaFaturadaService;
    private final RegistrarMudancaStatusService registrarMudancaStatusService;

    public AlimentarFaturaNaAprovacaoService(FaturaRepository faturaRepository,
                                             EspelhoRepository espelhoRepository,
                                             EspelhoItemRepository espelhoItemRepository,
                                             GuiaProcRepository guiaProcRepository,
                                             MontarItensFaturaService montarItensFaturaService,
                                             AtualizarStatusGuiaProcParaFaturadaService atualizarStatusGuiaProcParaFaturadaService,
                                             RegistrarMudancaStatusService registrarMudancaStatusService) {
        this.faturaRepository = faturaRepository;
        this.espelhoRepository = espelhoRepository;
        this.espelhoItemRepository = espelhoItemRepository;
        this.guiaProcRepository = guiaProcRepository;
        this.montarItensFaturaService = montarItensFaturaService;
        this.atualizarStatusGuiaProcParaFaturadaService = atualizarStatusGuiaProcParaFaturadaService;
        this.registrarMudancaStatusService = registrarMudancaStatusService;
    }

    // Uma fatura ATIVA por OCS e mês: a primeira aprovação abre, as seguintes só somam itens.
    @Transactional
    public FaturaModel processarAprovacaoEspelho(EspelhoModel espelho) {
        String competencia = espelho.getDataInicio().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        FaturaModel fatura = faturaRepository
                .findByOcsIdAndCompetenciaAndStatus(espelho.getOcsId(), competencia, StatusFatura.ATIVA)
                .orElseGet(() -> abrirFatura(espelho, competencia));

        montarItensFaturaService.montarItensFatura(espelho, fatura);

        List<EspelhoItemModel> itens = espelhoItemRepository.findByEspelhoId(espelho.getId());
        for (EspelhoItemModel item : itens) {
            GuiaProcModel atendimento = guiaProcRepository.findById(item.getAtendimentoId()).orElseThrow();
            atualizarStatusGuiaProcParaFaturadaService.atualizarStatusGuiaProcParaFaturada(atendimento);
        }

        String statusAnterior = espelho.getStatus().name();
        espelho.setStatus(StatusEspelho.FATURADO);
        espelhoRepository.save(espelho);
        registrarMudancaStatusService.executar(EntidadeTipo.ESPELHO, espelho.getId().longValue(),
                statusAnterior, StatusEspelho.FATURADO.name(), ORIGEM);
        return fatura;
    }

    private FaturaModel abrirFatura(EspelhoModel espelho, String competencia) {
        FaturaModel fatura = new FaturaModel();
        fatura.setOcsId(espelho.getOcsId());
        fatura.setCompetencia(competencia);
        fatura.setEspelhoModel(espelho);
        fatura.setGuiaProcModel(guiaProcRepository.findById(espelho.getAtendimentoId()).orElse(null));
        fatura.setStatus(StatusFatura.ATIVA);
        fatura.setDataGeracao(LocalDateTime.now());
        fatura = faturaRepository.save(fatura);
        registrarMudancaStatusService.executar(EntidadeTipo.FATURA, fatura.getId(),
                null, StatusFatura.ATIVA.name(), "aprovacao-do-espelho");
        return fatura;
    }
}
