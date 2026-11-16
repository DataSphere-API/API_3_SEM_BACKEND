package com.datasphere.fusex.fatura.service;

import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.encaminhamento.service.ValidarMesEncerradoService;
import com.datasphere.fusex.espelho.EspelhoModel;
import com.datasphere.fusex.espelho.EspelhoRepository;
import com.datasphere.fusex.espelhoItem.EspelhoItemModel;
import com.datasphere.fusex.espelhoItem.EspelhoItemRepository;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.fatura.FaturaModel;
import com.datasphere.fusex.fatura.FaturaRepository;
import com.datasphere.fusex.fatura.ItemFaturaModel;
import com.datasphere.fusex.fatura.ItemFaturaRepository;
import com.datasphere.fusex.fatura.StatusFatura;
import com.datasphere.fusex.fatura.dto.FaturaMensalDetalheResponse;
import com.datasphere.fusex.fatura.dto.FaturaMensalDetalheResponse.Espelho;
import com.datasphere.fusex.fatura.dto.FaturaMensalDetalheResponse.Item;
import com.datasphere.fusex.fatura.dto.FaturaMensalDetalheResponse.Pendente;
import com.datasphere.fusex.fatura.dto.FaturaMensalResumoResponse;
import com.datasphere.fusex.ocs.OcsModel;
import com.datasphere.fusex.ocs.OcsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ConsultarFaturaMensalService {

    private final FaturaRepository faturas;
    private final ItemFaturaRepository itensFatura;
    private final EspelhoRepository espelhos;
    private final EspelhoItemRepository itensEspelho;
    private final OcsRepository ocs;

    public ConsultarFaturaMensalService(FaturaRepository faturas, ItemFaturaRepository itensFatura,
                                       EspelhoRepository espelhos, EspelhoItemRepository itensEspelho,
                                       OcsRepository ocs) {
        this.faturas = faturas;
        this.itensFatura = itensFatura;
        this.espelhos = espelhos;
        this.itensEspelho = itensEspelho;
        this.ocs = ocs;
    }

    @Transactional(readOnly = true)
    public List<FaturaMensalResumoResponse> listar(String cnpj, YearMonth competencia, StatusFatura status) {
        List<FaturaModel> candidatas = status == null ? faturas.findAll() : faturas.findByStatus(status);
        return candidatas.stream()
                .filter(fatura -> cnpj == null || cnpj.equals(ocsDaFatura(fatura)))
                .filter(fatura -> competencia == null || competencia.equals(competenciaDaFatura(fatura)))
                .sorted(Comparator.comparing(FaturaModel::getId).reversed())
                .map(this::montarDetalhe)
                .map(FaturaMensalDetalheResponse::resumo)
                .toList();
    }

    @Transactional(readOnly = true)
    public FaturaMensalDetalheResponse buscar(Long id) {
        return montarDetalhe(faturas.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.FATURA_NAO_ENCONTRADA)));
    }

    public YearMonth competenciaDaFatura(FaturaModel fatura) {
        if (fatura.getCompetencia() != null) return YearMonth.parse(fatura.getCompetencia());
        if (fatura.getEspelhoModel() != null) return YearMonth.from(fatura.getEspelhoModel().getDataInicio());
        return fatura.getDataGeracao() == null ? null : YearMonth.from(fatura.getDataGeracao());
    }

    public List<Pendente> listarPendentes(FaturaModel fatura) {
        YearMonth competencia = competenciaDaFatura(fatura);
        String cnpj = ocsDaFatura(fatura);
        if (competencia == null || cnpj == null) return List.of();

        return espelhos.listarPendentesDaCompetencia(cnpj, competencia.atDay(1), competencia.plusMonths(1).atDay(1))
                .stream().map(espelho -> new Pendente(espelho.getEspelhoId(), espelho.getStatus()))
                .toList();
    }

    private String ocsDaFatura(FaturaModel fatura) {
        if (fatura.getOcsId() != null) return fatura.getOcsId();
        return fatura.getEspelhoModel() == null ? null : fatura.getEspelhoModel().getOcsId();
    }

    private FaturaMensalDetalheResponse montarDetalhe(FaturaModel fatura) {
        List<ItemFaturaModel> itens = itensFatura.findByFaturaId(fatura.getId());
        Map<Integer, EspelhoItemModel> referencias = itensEspelho.findAllById(
                itens.stream().map(ItemFaturaModel::getEspelhoItemId).toList())
                .stream().collect(Collectors.toMap(EspelhoItemModel::getId, Function.identity()));

        Map<Integer, List<Item>> porEspelho = new LinkedHashMap<>();
        BigDecimal apresentado = BigDecimal.ZERO;
        BigDecimal contratado = BigDecimal.ZERO;
        for (ItemFaturaModel item : itens) {
            EspelhoItemModel referencia = referencias.get(item.getEspelhoItemId());
            porEspelho.computeIfAbsent(referencia.getEspelhoId(), chave -> new ArrayList<>())
                    .add(new Item(item.getId(), referencia.getId(), referencia.getAtendimentoId(),
                            referencia.getDescricao(), referencia.getBeneficiarioId(), item.getValorApresentado(),
                            item.getValorContratado(), item.getDivergenciaValor()));
            apresentado = apresentado.add(item.getValorApresentado());
            contratado = contratado.add(item.getValorContratado());
        }

        List<Espelho> detalhe = espelhos.findAllById(porEspelho.keySet()).stream()
                .sorted(Comparator.comparing(EspelhoModel::getId))
                .map(espelho -> new Espelho(espelho.getId(), espelho.getDataInicio(), espelho.getDataFim(),
                        porEspelho.get(espelho.getId())))
                .toList();
        List<Pendente> pendentes = listarPendentes(fatura);
        String cnpj = ocsDaFatura(fatura);
        String nome = cnpj == null ? null : ocs.findById(cnpj).map(OcsModel::getNome).orElse(cnpj);
        YearMonth competencia = competenciaDaFatura(fatura);
        boolean podeEncaminhar = fatura.getStatus() == StatusFatura.ATIVA && competencia != null
                && ValidarMesEncerradoService.podeEncaminhar(competencia, LocalDate.now()) && pendentes.isEmpty();

        return new FaturaMensalDetalheResponse(new FaturaMensalResumoResponse(
                fatura.getId(), cnpj, nome, competencia == null ? null : competencia.toString(), fatura.getStatus(),
                detalhe.size(), itens.size(), apresentado, contratado, pendentes.size(), fatura.getDataGeracao(),
                fatura.getDataEncaminhamento(), podeEncaminhar), detalhe, pendentes);
    }
}
