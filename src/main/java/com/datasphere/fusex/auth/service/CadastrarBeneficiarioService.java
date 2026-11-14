package com.datasphere.fusex.auth.service;

import com.datasphere.fusex.auth.dto.CadastroRequest;
import com.datasphere.fusex.auth.dto.CadastroResponse;
import com.datasphere.fusex.beneficiario.BeneficiarioModel;
import com.datasphere.fusex.beneficiario.BeneficiarioRepository;
import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.usuario.UsuarioModel;
import com.datasphere.fusex.usuario.UsuarioRepository;
import com.datasphere.fusex.util.Documentos;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * POST /auth/cadastro: a pessoa cria o próprio login de beneficiário.
 * Se o Prec-CP já existe no banco sem login (carga do FUSEx), só liga o login a ele.
 */
@Service
public class CadastrarBeneficiarioService {

    private final UsuarioRepository usuarioRepository;
    private final BeneficiarioRepository beneficiarioRepository;
    private final ValidarPrecCpService validarPrecCpService;
    private final PasswordEncoder passwordEncoder;

    public CadastrarBeneficiarioService(UsuarioRepository usuarioRepository,
                                        BeneficiarioRepository beneficiarioRepository,
                                        ValidarPrecCpService validarPrecCpService,
                                        PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.beneficiarioRepository = beneficiarioRepository;
        this.validarPrecCpService = validarPrecCpService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public CadastroResponse cadastrar(CadastroRequest request) {
        String precCp = request.precCp() == null ? null : request.precCp().trim();
        String cpf = Documentos.somenteDigitos(request.cpf());

        validarPrecCpService.validarFormato(precCp);
        if (request.dependente()) {
            validarDependente(precCp, Documentos.somenteDigitos(request.cpfResponsavel()));
        } else if (!validarPrecCpService.ehTitular(precCp)) {
            throw new BusinessException(ErrorCode.PREC_CP_TITULAR_INVALIDO);
        }

        BeneficiarioModel existente = beneficiarioRepository.findByPrecCP(precCp).orElse(null);
        if (existente != null && existente.getUsuarioId() != null) {
            throw new BusinessException(ErrorCode.BENEFICIARIO_JA_CADASTRADO);
        }
        if (existente != null && !Objects.equals(existente.getDataNasc(), request.dataNascimento())) {
            throw new BusinessException(ErrorCode.BENEFICIARIO_NAO_CONFERE);
        }
        if (usuarioRepository.existsByLogin(cpf)) {
            throw new BusinessException(ErrorCode.CPF_JA_CADASTRADO);
        }

        UsuarioModel usuario = new UsuarioModel();
        usuario.setLogin(cpf);
        usuario.setSenha(passwordEncoder.encode(request.senha()));
        usuario = usuarioRepository.save(usuario);

        BeneficiarioModel beneficiario = existente;
        if (beneficiario == null) {
            beneficiario = novoBeneficiario(request, precCp);
        }
        beneficiario.setUsuarioId(usuario.getId());
        beneficiarioRepository.save(beneficiario);

        return new CadastroResponse(usuario.getId(), beneficiario.getNome(), beneficiario.getPrecCP());
    }

    private void validarDependente(String precCp, String cpfResponsavel) {
        if (validarPrecCpService.ehTitular(precCp)) {
            throw new BusinessException(ErrorCode.PREC_CP_DEPENDENTE_INVALIDO);
        }

        BeneficiarioModel titular = null;
        UsuarioModel responsavel = usuarioRepository.findByLogin(cpfResponsavel).orElse(null);
        if (responsavel != null) {
            titular = beneficiarioRepository.findByUsuarioId(responsavel.getId()).orElse(null);
        }
        if (titular == null || !validarPrecCpService.ehTitular(titular.getPrecCP())) {
            throw new BusinessException(ErrorCode.RESPONSAVEL_NAO_ENCONTRADO);
        }
        if (!validarPrecCpService.base(titular.getPrecCP()).equals(validarPrecCpService.base(precCp))) {
            throw new BusinessException(ErrorCode.PREC_CP_NAO_CONFERE);
        }
    }

    private BeneficiarioModel novoBeneficiario(CadastroRequest request, String precCp) {
        BeneficiarioModel beneficiario = new BeneficiarioModel();
        beneficiario.setPrecCP(precCp);
        beneficiario.setNome(request.nome().trim());
        beneficiario.setDataNasc(request.dataNascimento());
        beneficiario.setDependente(request.dependente());
        beneficiario.setGrupo(request.dependente() ? "Dependente" : "Titular");
        beneficiario.setCotista(false);
        return beneficiario;
    }
}