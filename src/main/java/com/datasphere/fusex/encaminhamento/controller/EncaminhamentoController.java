package com.datasphere.fusex.encaminhamento.controller;

import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.encaminhamento.dto.EncaminharFaturaResponse;
import com.datasphere.fusex.encaminhamento.service.EncaminharFaturaService;
import com.datasphere.fusex.encaminhamento.service.ValidarAuditoriaConcluidaService;
import com.datasphere.fusex.encaminhamento.service.ValidarFaturaEncaminhavelService;
import com.datasphere.fusex.encaminhamento.service.ValidarMesEncerradoService;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.fatura.FaturaModel;
import com.datasphere.fusex.fatura.service.BuscarFaturaPorIdService;
import com.datasphere.fusex.fatura.service.ListarEspelhosPendentesService;
import com.datasphere.fusex.lisura.dto.LisuraResponse;
import com.datasphere.fusex.lisura.service.GerarLisuraDaFaturaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
public class EncaminhamentoController {

    private final BuscarFaturaPorIdService buscarFaturaPorIdService;
    private final ValidarFaturaEncaminhavelService validarFaturaEncaminhavelService;
    private final GerarLisuraDaFaturaService gerarLisuraDaFaturaService;
    private final ValidarAuditoriaConcluidaService validarAuditoriaConcluidaService;
    private final EncaminharFaturaService encaminharFaturaService;
    private final ValidarMesEncerradoService validarMesEncerradoService;
    private final ListarEspelhosPendentesService listarEspelhosPendentesService;

    public EncaminhamentoController(BuscarFaturaPorIdService buscarFaturaPorIdService,
                                    ValidarFaturaEncaminhavelService validarFaturaEncaminhavelService,
                                    GerarLisuraDaFaturaService gerarLisuraDaFaturaService,
                                    ValidarAuditoriaConcluidaService validarAuditoriaConcluidaService,
                                    EncaminharFaturaService encaminharFaturaService,
                                    ValidarMesEncerradoService validarMesEncerradoService,
                                    ListarEspelhosPendentesService listarEspelhosPendentesService) {
        this.buscarFaturaPorIdService = buscarFaturaPorIdService;
        this.validarFaturaEncaminhavelService = validarFaturaEncaminhavelService;
        this.gerarLisuraDaFaturaService = gerarLisuraDaFaturaService;
        this.validarAuditoriaConcluidaService = validarAuditoriaConcluidaService;
        this.encaminharFaturaService = encaminharFaturaService;
        this.validarMesEncerradoService = validarMesEncerradoService;
        this.listarEspelhosPendentesService = listarEspelhosPendentesService;
    }

    @PostMapping("/faturas/{id}/encaminhar")
    public ResponseEntity<EncaminharFaturaResponse> encaminharFatura(@PathVariable Long id) {
        FaturaModel fatura = buscarFaturaPorIdService.buscarFaturaPorId(id);

        if (fatura == null) {
            throw new BusinessException(ErrorCode.FATURA_NAO_ENCONTRADA);
        }

        if (!validarFaturaEncaminhavelService.validarFaturaEncaminhavel(fatura)) {
            throw new BusinessException(ErrorCode.FATURA_NAO_ENCAMINHAVEL);
        }

        validarMesEncerradoService.validarMesEncerrado(fatura.getCompetencia(), LocalDate.now());

        if (!listarEspelhosPendentesService.listar(fatura).isEmpty()) {
            throw new BusinessException(ErrorCode.ESPELHO_PENDENTE);
        }

        LisuraResponse lisura = gerarLisuraDaFaturaService.gerarLisuraDaFatura(fatura);

        if (!validarAuditoriaConcluidaService.validarAuditoriaConcluida(lisura)) {
            throw new BusinessException(ErrorCode.AUDITORIA_PENDENTE);
        }

        return ResponseEntity.ok(encaminharFaturaService.encaminharFatura(fatura));
    }
}