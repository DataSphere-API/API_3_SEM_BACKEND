package com.datasphere.fusex.guia.controller;

import com.datasphere.fusex.atendimento.service.ValidarDonoDaGuiaService;
import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.guia.StatusPainelBeneficiario;
import com.datasphere.fusex.preguia.PreGuiaRepository;
import com.datasphere.fusex.preguia.StatusPreGuia;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@RestController
public class BeneficiarioPreGuiaController {

    private final ValidarDonoDaGuiaService validarDono;
    private final PreGuiaRepository preGuias;

    public BeneficiarioPreGuiaController(ValidarDonoDaGuiaService validarDono, PreGuiaRepository preGuias) {
        this.validarDono = validarDono;
        this.preGuias = preGuias;
    }

    @GetMapping("/beneficiarios/{precCp}/pre-guias")
    public List<PreGuiaSolicitadaResponse> listar(@PathVariable String precCp, Principal principal) {
        if (!validarDono.identificarBeneficiario(principal).equals(precCp)) {
            throw new BusinessException(ErrorCode.BENEFICIARIO_SEM_PERMISSAO);
        }

        return preGuias.findByBenfIdAndGuiaIdIsNullAndStatusNotOrderByIdDesc(precCp, StatusPreGuia.EMITIDA)
                .stream().map(guia -> new PreGuiaSolicitadaResponse(
                        guia.getId(), StatusPainelBeneficiario.SOLICITADA, guia.getDataCriacao()))
                .toList();
    }

    public record PreGuiaSolicitadaResponse(Long preGuiaId, StatusPainelBeneficiario statusPainel,
                                            LocalDate dataCriacao) {}
}
