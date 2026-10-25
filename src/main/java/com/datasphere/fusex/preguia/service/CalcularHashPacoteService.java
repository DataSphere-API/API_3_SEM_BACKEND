package com.datasphere.fusex.preguia.service;

import com.datasphere.fusex.preguia.dto.PacoteSireResponse;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@Service
public class CalcularHashPacoteService {

    public String calcularHash(Long preGuiaId, PacoteSireResponse.Beneficiario beneficiario,
                               PacoteSireResponse.Medico medico, PacoteSireResponse.AprovadoPor aprovadoPor,
                               List<PacoteSireResponse.Procedimento> procedimentos) {
        StringBuilder conteudo = new StringBuilder();
        conteudo.append(preGuiaId).append("|");
        conteudo.append(beneficiario.precCp()).append("|");
        conteudo.append(medico.crm()).append("|");
        conteudo.append(aprovadoPor.chefeId()).append("|");
        conteudo.append(aprovadoPor.dataAprovacaoMedico()).append("|");
        conteudo.append(aprovadoPor.dataAprovacaoChefe()).append("|");
        for (PacoteSireResponse.Procedimento procedimento : procedimentos) {
            conteudo.append(procedimento.atendimentoId()).append(":");
            conteudo.append(procedimento.ocsCnpj()).append(":");
            conteudo.append(procedimento.qtdSessoes()).append(":");
            conteudo.append(procedimento.valorUnitario()).append("|");
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(conteudo.toString().getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : bytes) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponível", e);
        }
    }
}