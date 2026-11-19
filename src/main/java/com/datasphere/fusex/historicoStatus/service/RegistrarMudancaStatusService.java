package com.datasphere.fusex.historicoStatus.service;

import com.datasphere.fusex.historicoStatus.EntidadeTipo;
import com.datasphere.fusex.historicoStatus.HistoricoStatusModel;
import com.datasphere.fusex.historicoStatus.HistoricoStatusRepository;
import com.datasphere.fusex.auth.UsuarioLogado;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class RegistrarMudancaStatusService {

    private final HistoricoStatusRepository repository;

    public RegistrarMudancaStatusService(HistoricoStatusRepository repository) {
        this.repository = repository;
    }

    public void executar(EntidadeTipo tipo, Long entidadeId,
                         String statusAnterior, String statusNovo,
                         String origem) {
        HistoricoStatusModel evento = new HistoricoStatusModel(
                tipo, entidadeId, statusAnterior, statusNovo, origem);
        var autenticacao = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacao != null && autenticacao.isAuthenticated()
                && autenticacao.getPrincipal() instanceof UsuarioLogado usuario) {
            evento.setUsuarioId(usuario.id());
            evento.setUsuarioNome(usuario.nome());
        }
        repository.save(evento);
    }
}
