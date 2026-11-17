package com.datasphere.fusex.auditor.service;

import com.datasphere.fusex.auditor.AuditorRepository;
import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.exception.BusinessException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IdentificarAuditorServiceTest {

    @Mock
    private AuditorRepository auditorRepository;

    @InjectMocks
    private IdentificarAuditorService service;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void auditorExistentePorHeader_retornaId() {
        when(auditorRepository.existsById(2)).thenReturn(true);

        Integer id = service.identificarAuditor(2);
        assertEquals(2, id);
    }

    @Test
    void auditorInexistenteNoBanco_lanca403() {
        when(auditorRepository.existsById(99)).thenReturn(false);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.identificarAuditor(99));
        assertEquals(ErrorCode.AUDITOR_INVALIDO, ex.getErrorCode());
    }

    @Test
    void semHeaderESemUsuarioLogado_lanca403() {
        BusinessException ex = assertThrows(BusinessException.class, () -> service.identificarAuditor(null));
        assertEquals(ErrorCode.AUDITOR_INVALIDO, ex.getErrorCode());
    }
}
