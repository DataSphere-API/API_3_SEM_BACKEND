package com.datasphere.fusex.usuario.service;

import com.datasphere.fusex.analista.AnalistaModel;
import com.datasphere.fusex.analista.AnalistaRepository;
import com.datasphere.fusex.auditor.AuditorModel;
import com.datasphere.fusex.auditor.AuditorRepository;
import com.datasphere.fusex.beneficiario.BeneficiarioRepository;
import com.datasphere.fusex.chefe.ChefeFusexModel;
import com.datasphere.fusex.chefe.ChefeFusexRepository;
import com.datasphere.fusex.chefe.service.ValidarChefeService;
import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.medico.MedicoModel;
import com.datasphere.fusex.medico.MedicoRepository;
import com.datasphere.fusex.ocs.FunOcsModel;
import com.datasphere.fusex.ocs.FunOcsRepository;
import com.datasphere.fusex.ocs.OcsRepository;
import com.datasphere.fusex.usuario.UsuarioModel;
import com.datasphere.fusex.usuario.UsuarioRepository;
import com.datasphere.fusex.usuario.dto.UsuarioRequest;
import com.datasphere.fusex.usuario.dto.UsuarioResponse;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.Set;

@Service
public class CadastrarUsuarioService {

    private static final Set<String> PERFIS_MILITARES = Set.of("CHEFE", "MEDICO", "EMISSOR", "AUDITOR");

    private final UsuarioRepository usuarioRepository;
    private final BeneficiarioRepository beneficiarioRepository;
    private final OcsRepository ocsRepository;
    private final FunOcsRepository funOcsRepository;
    private final ChefeFusexRepository chefeRepository;
    private final MedicoRepository medicoRepository;
    private final AnalistaRepository analistaRepository;
    private final AuditorRepository auditorRepository;
    private final ValidarChefeService validarChefeService;
    private final MontarUsuarioResponseService montarUsuarioResponseService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public CadastrarUsuarioService(UsuarioRepository usuarioRepository,
                                    BeneficiarioRepository beneficiarioRepository,
                                    OcsRepository ocsRepository, FunOcsRepository funOcsRepository,
                                    ChefeFusexRepository chefeRepository, MedicoRepository medicoRepository,
                                    AnalistaRepository analistaRepository, AuditorRepository auditorRepository,
                                    ValidarChefeService validarChefeService,
                                    MontarUsuarioResponseService montarUsuarioResponseService) {
        this.usuarioRepository = usuarioRepository;
        this.beneficiarioRepository = beneficiarioRepository;
        this.ocsRepository = ocsRepository;
        this.funOcsRepository = funOcsRepository;
        this.chefeRepository = chefeRepository;
        this.medicoRepository = medicoRepository;
        this.analistaRepository = analistaRepository;
        this.auditorRepository = auditorRepository;
        this.validarChefeService = validarChefeService;
        this.montarUsuarioResponseService = montarUsuarioResponseService;
    }

    @Transactional
    public UsuarioResponse cadastrar(UsuarioRequest request, Principal principal) {
        validarChefeService.validar(principal);
        if (request == null || request.tipo() == null) {
            throw new BusinessException(ErrorCode.DADOS_INVALIDOS);
        }
        return switch (request.tipo()) {
            case "MILITAR" -> cadastrarMilitar(request);
            case "OCS" -> cadastrarOcs(request);
            default -> throw new BusinessException(ErrorCode.DADOS_INVALIDOS);
        };
    }

    private UsuarioResponse cadastrarMilitar(UsuarioRequest request) {
        if (request.precCp() == null || request.precCp().isBlank()) {
            throw new BusinessException(ErrorCode.DADOS_INVALIDOS);
        }
        var beneficiario = beneficiarioRepository.buscarParaAtualizar(request.precCp().trim())
                .orElseThrow(() -> new BusinessException(ErrorCode.BENEFICIARIO_NAO_ENCONTRADO));
        if (request.perfis() == null || request.perfis().isEmpty()
                || request.perfis().stream().anyMatch(p -> p == null || !PERFIS_MILITARES.contains(p))) {
            throw new BusinessException(ErrorCode.PERFIL_OBRIGATORIO);
        }
        boolean medico = request.perfis().contains("MEDICO");
        if (medico && (request.crm() == null || request.crm().isBlank())) {
            throw new BusinessException(ErrorCode.MEDICO_SEM_CRM);
        }
        if (medico && request.crm().trim().length() > 20) {
            throw new BusinessException(ErrorCode.DADOS_INVALIDOS);
        }
        if (beneficiario.getUsuarioId() == null) {
            throw new BusinessException(ErrorCode.PRIMEIRO_ACESSO_PENDENTE);
        }
        var usuario = usuarioRepository.findById(beneficiario.getUsuarioId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PRIMEIRO_ACESSO_PENDENTE));
        String precCp = beneficiario.getPrecCP();
        if (medico && medicoRepository.existsByCrmAndBenefIdNot(request.crm().trim(), precCp)) {
            throw new BusinessException(ErrorCode.CRM_JA_CADASTRADO);
        }
        if (request.perfis().contains("CHEFE") && chefeRepository.findByBenefId(precCp).isEmpty()) {
            var chefe = new ChefeFusexModel();
            chefe.setBenefId(precCp);
            chefeRepository.save(chefe);
        }
        if (medico) {
            var perfil = medicoRepository.findByBenefId(precCp).orElseGet(MedicoModel::new);
            perfil.setBenefId(precCp);
            perfil.setCrm(request.crm().trim());
            medicoRepository.saveAndFlush(perfil);
        }
        if (request.perfis().contains("EMISSOR") && analistaRepository.findByBenefId(precCp).isEmpty()) {
            var analista = new AnalistaModel();
            analista.setBenefId(precCp);
            analistaRepository.save(analista);
        }
        if (request.perfis().contains("AUDITOR") && auditorRepository.findByBenefId(precCp).isEmpty()) {
            var auditor = new AuditorModel();
            auditor.setBenefId(precCp);
            auditorRepository.save(auditor);
        }
        return montarUsuarioResponseService.montar(usuario);
    }

    private UsuarioResponse cadastrarOcs(UsuarioRequest request) {
        if (request.cpf() == null || request.senha() == null || request.ocsCnpj() == null
                || request.senha().isBlank() || request.senha().length() < 8
                || request.senha().getBytes(StandardCharsets.UTF_8).length > 72
                || (request.cargo() != null && request.cargo().trim().length() > 80)) {
            throw new BusinessException(ErrorCode.DADOS_INVALIDOS);
        }
        String cpf = request.cpf().replaceAll("[.\\-/\\s]", "");
        String cnpj = request.ocsCnpj().replaceAll("[.\\-/\\s]", "");
        if (!cpf.matches("[0-9]{11}")) {
            throw new BusinessException(ErrorCode.DADOS_INVALIDOS);
        }
        var ocs = ocsRepository.findById(cnpj)
                .orElseThrow(() -> new BusinessException(ErrorCode.OCS_NAO_ENCONTRADA));
        if (usuarioRepository.existsByLogin(cpf)) {
            throw new BusinessException(ErrorCode.CPF_JA_CADASTRADO);
        }
        var usuario = new UsuarioModel();
        usuario.setLogin(cpf);
        usuario.setSenha(passwordEncoder.encode(request.senha()));
        usuarioRepository.saveAndFlush(usuario);
        var funcionario = new FunOcsModel();
        funcionario.setUsuarioId(usuario.getId());
        funcionario.setOcsModel(ocs);
        funcionario.setCargo(request.cargo() == null ? null : request.cargo().trim());
        funOcsRepository.saveAndFlush(funcionario);
        return montarUsuarioResponseService.montar(usuario);
    }
}
