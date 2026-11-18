package com.datasphere.fusex.preguia.service;

import com.datasphere.fusex.preguia.dto.PacoteSireResponse;
import com.datasphere.fusex.preguia.pdf.GeradorPdfSimples;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class GerarDocumentoConferenciaService {

    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final MontarPacoteSireService montarPacoteSireService;

    public GerarDocumentoConferenciaService(MontarPacoteSireService montarPacoteSireService) {
        this.montarPacoteSireService = montarPacoteSireService;
    }

    public byte[] gerarDocumento(Long preGuiaId) {
        return gerarDocumento(montarPacoteSireService.montarPacote(preGuiaId));
    }

    public byte[] gerarDocumento(PacoteSireResponse pacote) {
        return GeradorPdfSimples.gerar(montarLinhas(pacote));
    }

    List<String> montarLinhas(PacoteSireResponse pacote) {
        List<String> linhas = new ArrayList<>();
        linhas.add("FUSEX - FUNDO DE SAUDE DO EXERCITO");
        linhas.add("DOCUMENTO DE CONFERENCIA DA PRE-GUIA No " + pacote.preGuiaId());
        linhas.add("Situacao da validacao: " + pacote.status());
        linhas.add("Gerado em: " + pacote.geradoEm());
        linhas.add("");
        linhas.add("BENEFICIARIO");
        linhas.add("Nome: " + pacote.beneficiario().nome());
        linhas.add("Prec-CP: " + pacote.beneficiario().precCp());
        linhas.add("Data de nascimento: " + pacote.beneficiario().dataNascimento());
        linhas.add("");
        linhas.add("MEDICO RESPONSAVEL");
        linhas.add("Nome: " + pacote.medico().nome());
        linhas.add("CRM: " + pacote.medico().crm());
        linhas.add("");
        linhas.add("PROCEDIMENTOS");
        linhas.add(ajustar("Procedimento", 28) + ajustar("OCS", 24) + ajustar("Sessoes", 8) + "Valor");
        for (PacoteSireResponse.Procedimento p : pacote.procedimentos()) {
            linhas.add(ajustar(p.nome(), 28) + ajustar(p.ocsNome(), 24) + ajustar(String.valueOf(p.qtdSessoes()), 8)
                    + p.valorTotal());
        }
        linhas.add("");
        linhas.add("Valor total: " + pacote.valorTotal());
        linhas.add("Validade da guia: " + pacote.validadeDias() + " dias a partir da emissao");
        linhas.add("");
        linhas.add("Hash (SHA-256):");
        linhas.add(pacote.hash().substring(0, 32));
        linhas.add(pacote.hash().substring(32));
        linhas.add("");
        linhas.add("APROVACAO");
        linhas.addAll(montarLinhasDaAssinatura(pacote.aprovadoPor()));
        return linhas;
    }

    private List<String> montarLinhasDaAssinatura(PacoteSireResponse.AprovadoPor aprovadoPor) {
        List<String> linhas = new ArrayList<>();
        if (aprovadoPor == null || aprovadoPor.dataHora() == null) {
            linhas.add("Sem aprovacao registrada.");
            return linhas;
        }
        String cargo = "Chefe do FUSEX";
        if ("MEDICO".equals(aprovadoPor.perfil())) {
            cargo = "Medico responsavel";
        }
        linhas.add("Aprovada por: " + aprovadoPor.nome() + " (" + cargo + ")");
        linhas.add("Usuario: " + aprovadoPor.usuarioId());
        linhas.add("Data e hora: " + aprovadoPor.dataHora().format(DATA_HORA));
        return linhas;
    }

    private String ajustar(String texto, int tamanho) {
        String valor = texto;
        if (valor == null) {
            valor = "";
        }
        if (valor.length() > tamanho - 1) {
            valor = valor.substring(0, tamanho - 1);
        }
        StringBuilder resultado = new StringBuilder(valor);
        while (resultado.length() < tamanho) {
            resultado.append(' ');
        }
        return resultado.toString();
    }
}
