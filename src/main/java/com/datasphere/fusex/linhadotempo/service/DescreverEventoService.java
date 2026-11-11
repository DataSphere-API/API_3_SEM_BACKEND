package com.datasphere.fusex.linhadotempo.service;

import org.springframework.stereotype.Service;

@Service
public class DescreverEventoService {

    public String descrever(String statusAnterior, String statusNovo, String origem) {
        return switch (origem) {
            case "validacao-pre-guia" -> "COM_PENDENCIAS".equals(statusNovo)
                    ? "Pré-guia validada com pendências"
                    : "Pré-guia validada e enviada para aprovação";
            case "aprovacao-chefe" -> "Pré-guia aprovada pelo Chefe do FUSEx";
            case "reprovacao-pre-guia" -> "Pré-guia reprovada";
            case "emissao-da-guia" -> "Guia emitida";
            case "validacao-qrcode" -> "Atendimento validado por QR Code";
            case "expiracao-de-guias" -> "Prazo de validade encerrado (expirada)";
            case "envio-pela-ocs" -> "Espelho enviado pela OCS";
            case "reenvio-pela-ocs" -> "Espelho corrigido e reenviado pela OCS";
            case "auditoria" -> "Espelho analisado pela auditoria";
            case "regeracao-por-espelho-corrigido" -> "Fatura gerada a partir do espelho corrigido";
            case "encaminhamento-para-aprovacao" -> "Fatura encaminhada para aprovação";
            default -> statusAnterior == null
                    ? "Status definido como " + statusNovo
                    : "Status alterado de " + statusAnterior + " para " + statusNovo;
        };
    }
}