package com.datasphere.fusex.espelho.service;

import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.espelho.dto.ItemEspelhoRequest;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.guiaproc.GuiaProcRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ValidarBeneficiarioDaGuiaService {

    private final GuiaProcRepository guiaProcRepository;

    public ValidarBeneficiarioDaGuiaService(GuiaProcRepository guiaProcRepository) {
        this.guiaProcRepository = guiaProcRepository;
    }

    public void validarBeneficiarios(Long atendimentoId, List<ItemEspelhoRequest> itens) {
        String benfIdEsperado = guiaProcRepository.findBeneficiarioIdPorAtendimentoId(atendimentoId)
                .orElse(null);

        List<String> detalhes = new ArrayList<>();
        if (itens != null) {
            for (int i = 0; i < itens.size(); i++) {
                ItemEspelhoRequest item = itens.get(i);
                if (item.beneficiarioId() == null
                        || benfIdEsperado == null
                        || !item.beneficiarioId().trim().equalsIgnoreCase(benfIdEsperado.trim())) {
                    detalhes.add("itens[" + i + "].beneficiarioId: beneficiário não pertence à guia do atendimento");
                }
            }
        }

        if (!detalhes.isEmpty()) {
            throw new BusinessException(ErrorCode.BENEFICIARIO_FORA_DA_GUIA, detalhes);
        }
    }

    public void validarBeneficiarioItem(Long atendimentoId, String beneficiarioId) {
        String benfIdEsperado = guiaProcRepository.findBeneficiarioIdPorAtendimentoId(atendimentoId)
                .orElse(null);

        if (beneficiarioId == null
                || benfIdEsperado == null
                || !beneficiarioId.trim().equalsIgnoreCase(benfIdEsperado.trim())) {
            throw new BusinessException(
                    ErrorCode.BENEFICIARIO_FORA_DA_GUIA,
                    List.of("beneficiarioId: beneficiário não pertence à guia do atendimento")
            );
        }
    }
}
