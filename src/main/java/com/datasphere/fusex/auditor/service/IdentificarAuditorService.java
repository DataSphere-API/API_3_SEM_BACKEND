package com.datasphere.fusex.auditor.service;

import com.datasphere.fusex.auditor.AuditorRepository;
import com.datasphere.fusex.auth.UsuarioLogado;
import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.exception.BusinessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class IdentificarAuditorService {

    private final AuditorRepository auditorRepository;

    public IdentificarAuditorService(AuditorRepository auditorRepository) {
        this.auditorRepository = auditorRepository;
    }

    public Integer identificarAuditor(Integer headerAuditorId) {
        Integer auditorId = headerAuditorId;

        if (auditorId == null) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof UsuarioLogado usuario) {
                auditorId = usuario.auditorId();
            }
        }

        if (auditorId == null) {
            throw new BusinessException(ErrorCode.AUDITOR_INVALIDO);
        }

        if (!auditorRepository.existsById(auditorId)) {
            throw new BusinessException(ErrorCode.AUDITOR_INVALIDO);
        }

        return auditorId;
    }
}
