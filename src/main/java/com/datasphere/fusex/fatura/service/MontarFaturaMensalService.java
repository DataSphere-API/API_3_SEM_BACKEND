package com.datasphere.fusex.fatura.service;

import com.datasphere.fusex.encaminhamento.service.ValidarAuditoriaConcluidaService;
import com.datasphere.fusex.encaminhamento.service.ValidarMesEncerradoService;
import com.datasphere.fusex.espelho.EspelhoModel;
import com.datasphere.fusex.espelho.EspelhoRepository;
import com.datasphere.fusex.espelhoitem.EspelhoItemModel;
import com.datasphere.fusex.espelhoitem.EspelhoItemRepository;
import com.datasphere.fusex.fatura.FaturaModel;
import com.datasphere.fusex.fatura.ItemFaturaModel;
import com.datasphere.fusex.fatura.ItemFaturaRepository;
import com.datasphere.fusex.fatura.StatusFatura;
import com.datasphere.fusex.fatura.dto.FaturaMensalDetalheResponse;
import com.datasphere.fusex.fatura.dto.FaturaMensalResumoResponse;
import com.datasphere.fusex.lisura.service.GerarLisuraDaFaturaService;
import com.datasphere.fusex.ocs.OcsModel;
import com.datasphere.fusex.ocs.OcsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class MontarFaturaMensalService {

    private final ItemFaturaRepository itemFaturaRepository;
    private final EspelhoItemRepository espelhoItemRepository;
    private final EspelhoRepository espelhoRepository;
    private final OcsRepository ocsRepository;
    private final ListarEspelhosPendentesService listarEspelhosPendentesService;
    private final ValidarMesEncerradoService validarMesEncerradoService;
    private final GerarLisuraDaFaturaService gerarLisuraDaFaturaService;
    private final ValidarAuditoriaConcluidaService validarAuditoriaConcluidaService;

    public MontarFaturaMensalService(ItemFaturaRepository itemFaturaRepository,
                                     EspelhoItemRepository espelhoItemRepository,
                                     EspelhoRepository espelhoRepository,
                                     OcsRepository ocsRepository,
                                     ListarEspelhosPendentesService listarEspelhosPendentesService,
                                     ValidarMesEncerradoService validarMesEncerradoService,
                                     GerarLisuraDaFaturaService gerarLisuraDaFaturaService,
                                     ValidarAuditoriaConcluidaService validarAuditoriaConcluidaService) {
        this.itemFaturaRepository = itemFaturaRepository;
        this.espelhoItemRepository = espelhoItemRepository;
        this.espelhoRepository = espelhoRepository;
        this.ocsRepository = ocsRepository;
        this.listarEspelhosPendentesService = listarEspelhosPendentesService;
        this.validarMesEncerradoService = validarMesEncerradoService;
        this.gerarLisuraDaFaturaService = gerarLisuraDaFaturaService;
        this.validarAuditoriaConcluidaService = validarAuditoriaConcluidaService;
    }

    @Transactional(readOnly = true)
    public FaturaMensalDetalheResponse montarDetalhe(FaturaModel fatura) {
        List<ItemFaturaModel> itensDaFatura = itemFaturaRepository.findByFaturaId(fatura.getId());

        // Agrupa os itens da fatura pelo espelho de onde vieram, na ordem em que entraram.
        Map<Integer, List<FaturaMensalDetalheResponse.Item>> itensPorEspelho = new LinkedHashMap<>();
        BigDecimal valorApresentado = BigDecimal.ZERO;
        BigDecimal valorContratado = BigDecimal.ZERO;
        for (ItemFaturaModel itemDaFatura : itensDaFatura) {
            EspelhoItemModel itemDoEspelho = espelhoItemRepository.findById(itemDaFatura.getEspelhoItemId()).orElse(null);
            if (itemDoEspelho == null) {
                continue;
            }
            itensPorEspelho.computeIfAbsent(itemDoEspelho.getEspelhoId(), id -> new ArrayList<>())
                    .add(new FaturaMensalDetalheResponse.Item(
                            itemDaFatura.getId(),
                            itemDoEspelho.getId(),
                            itemDoEspelho.getAtendimentoId(),
                            itemDoEspelho.getDescricao(),
                            itemDoEspelho.getBeneficiarioId(),
                            itemDaFatura.getValorApresentado(),
                            itemDaFatura.getValorContratado(),
                            itemDaFatura.getDivergenciaValor()));
            valorApresentado = valorApresentado.add(itemDaFatura.getValorApresentado());
            valorContratado = valorContratado.add(itemDaFatura.getValorContratado());
        }

        List<FaturaMensalDetalheResponse.Espelho> espelhos = new ArrayList<>();
        for (Map.Entry<Integer, List<FaturaMensalDetalheResponse.Item>> grupo : itensPorEspelho.entrySet()) {
            EspelhoModel espelho = espelhoRepository.findById(grupo.getKey()).orElse(null);
            espelhos.add(new FaturaMensalDetalheResponse.Espelho(
                    grupo.getKey(),
                    espelho == null ? null : espelho.getDataInicio(),
                    espelho == null ? null : espelho.getDataFim(),
                    grupo.getValue()));
        }

        List<FaturaMensalDetalheResponse.Pendente> pendentes = new ArrayList<>();
        for (EspelhoModel espelho : listarEspelhosPendentesService.listar(fatura)) {
            pendentes.add(new FaturaMensalDetalheResponse.Pendente(espelho.getId(), espelho.getStatus()));
        }

        return new FaturaMensalDetalheResponse(
                fatura.getId(),
                fatura.getOcsId(),
                nomeDaOcs(fatura.getOcsId()),
                fatura.getCompetencia(),
                fatura.getStatus(),
                espelhos.size(),
                itensDaFatura.size(),
                valorApresentado,
                valorContratado,
                pendentes.size(),
                fatura.getDataGeracao(),
                fatura.getDataEncaminhamento(),
                podeEncaminhar(fatura, pendentes.isEmpty()),
                espelhos,
                pendentes);
    }

    @Transactional(readOnly = true)
    public FaturaMensalResumoResponse montarResumo(FaturaModel fatura) {
        FaturaMensalDetalheResponse detalhe = montarDetalhe(fatura);
        return new FaturaMensalResumoResponse(
                detalhe.id(),
                detalhe.ocsId(),
                detalhe.ocsNome(),
                detalhe.competencia(),
                detalhe.status(),
                detalhe.quantidadeEspelhos(),
                detalhe.quantidadeItens(),
                detalhe.valorApresentado(),
                detalhe.valorContratado(),
                detalhe.espelhosPendentes(),
                detalhe.dataGeracao(),
                detalhe.dataEncaminhamento(),
                detalhe.podeEncaminhar());
    }

    // As mesmas regras do POST /faturas/{id}/encaminhar, para a tela saber se mostra o botão.
    private boolean podeEncaminhar(FaturaModel fatura, boolean semPendentes) {
        return fatura.getStatus() == StatusFatura.ATIVA
                && validarMesEncerradoService.mesEncerrado(fatura.getCompetencia(), LocalDate.now())
                && semPendentes
                && validarAuditoriaConcluidaService.validarAuditoriaConcluida(
                        gerarLisuraDaFaturaService.gerarLisuraDaFatura(fatura));
    }

    private String nomeDaOcs(String cnpj) {
        if (cnpj == null) {
            return null;
        }
        return ocsRepository.findById(cnpj).map(OcsModel::getNome).orElse(cnpj);
    }
}
