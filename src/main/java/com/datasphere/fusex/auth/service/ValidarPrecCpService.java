package com.datasphere.fusex.auth.service;

import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.exception.BusinessException;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

// Prec-CP: 9 dígitos, hífen e 2 dígitos. Titular termina em -00; dependente usa o mesmo início com -01, -02...
@Service
public class ValidarPrecCpService {

    private static final Pattern FORMATO = Pattern.compile("^\\d{9}-\\d{2}$");
    private static final String SUFIXO_TITULAR = "-00";

    public void validarFormato(String precCp) {
        if (precCp == null || !FORMATO.matcher(precCp).matches()) {
            throw new BusinessException(ErrorCode.PREC_CP_INVALIDO);
        }
    }

    public boolean ehTitular(String precCp) {
        return precCp != null && precCp.endsWith(SUFIXO_TITULAR);
    }

    public String base(String precCp) {
        return precCp.substring(0, 9);
    }
}