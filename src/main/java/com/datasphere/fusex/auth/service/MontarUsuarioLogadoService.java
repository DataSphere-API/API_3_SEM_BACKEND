package com.datasphere.fusex.auth.service;

import com.datasphere.fusex.analista.AnalistaModel;
import com.datasphere.fusex.analista.AnalistaRepository;
import com.datasphere.fusex.auditor.AuditorModel;
import com.datasphere.fusex.auditor.AuditorRepository;
import com.datasphere.fusex.auth.Perfil;
import com.datasphere.fusex.auth.UsuarioLogado;
import com.datasphere.fusex.beneficiario.BeneficiarioModel;
import com.datasphere.fusex.beneficiario.BeneficiarioRepository;
import com.datasphere.fusex.chefe.ChefeFusexModel;
import com.datasphere.fusex.chefe.ChefeFusexRepository;
import com.datasphere.fusex.medico.MedicoModel;
import com.datasphere.fusex.medico.MedicoRepository;
import com.datasphere.fusex.ocs.FunOcsModel;
import com.datasphere.fusex.ocs.FunOcsRepository;
import com.datasphere.fusex.usuario.UsuarioModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.Set;

// Descobre os perfis olhando em qual tabela o usuário (ou o Prec-CP dele) aparece.
@Service
public class MontarUsuarioLogadoService {

    private final BeneficiarioRepository beneficiarioRepository;
    private final MedicoRepository medicoRepository;
    private final ChefeFusexRepository chefeFusexRepository;
    private final AnalistaRepository analistaRepository;
    private final AuditorRepository auditorRepository;
    private final FunOcsRepository funOcsRepository;

    public MontarUsuarioLogadoService(BeneficiarioRepository beneficiarioRepository,
                                      MedicoRepository medicoRepository,
                                      ChefeFusexRepository chefeFusexRepository,
                                      AnalistaRepository analistaRepository,
                                      AuditorRepository auditorRepository,
                                      FunOcsRepository funOcsRepository) {
        this.beneficiarioRepository = beneficiarioRepository;
        this.medicoRepository = medicoRepository;
        this.chefeFusexRepository = chefeFusexRepository;
        this.analistaRepository = analistaRepository;
        this.auditorRepository = auditorRepository;
        this.funOcsRepository = funOcsRepository;
    }

    @Transactional(readOnly = true)
    public UsuarioLogado montar(UsuarioModel usuario) {
        Set<Perfil> perfis = EnumSet.noneOf(Perfil.class);
        String nome = null;
        String precCp = null;
        Integer medicoId = null;
        String crm = null;
        Integer chefeId = null;
        Integer analistaId = null;
        Integer auditorId = null;
        String ocsCnpj = null;
        String cargo = null;

        BeneficiarioModel beneficiario = beneficiarioRepository.findByUsuarioId(usuario.getId()).orElse(null);
        if (beneficiario != null) {
            perfis.add(Perfil.BENEFICIARIO);
            precCp = beneficiario.getPrecCP();
            nome = beneficiario.getNome();

            MedicoModel medico = medicoRepository.findByBenefId(precCp).orElse(null);
            if (medico != null) {
                perfis.add(Perfil.MEDICO);
                medicoId = medico.getId();
                crm = medico.getCrm();
            }
            ChefeFusexModel chefe = chefeFusexRepository.findByBenefId(precCp).orElse(null);
            if (chefe != null) {
                perfis.add(Perfil.CHEFE);
                chefeId = chefe.getId().intValue();
            }
            AnalistaModel analista = analistaRepository.findByBenefId(precCp).orElse(null);
            if (analista != null) {
                perfis.add(Perfil.EMISSOR);
                analistaId = analista.getId();
            }
            AuditorModel auditor = auditorRepository.findByBenefId(precCp).orElse(null);
            if (auditor != null) {
                perfis.add(Perfil.AUDITOR);
                auditorId = auditor.getId();
            }
        }

        FunOcsModel funcionario = funOcsRepository.findByUsuarioId(usuario.getId()).orElse(null);
        if (funcionario != null) {
            perfis.add(Perfil.OCS);
            ocsCnpj = funcionario.getOcsModel().getCnpj();
            cargo = funcionario.getCargo();
            if (nome == null) {
                nome = montarNomeDoFuncionario(funcionario);
            }
        }

        if (nome == null) {
            nome = usuario.getLogin();
        }

        return new UsuarioLogado(usuario.getId(), usuario.getLogin(), nome, perfis, precCp,
                medicoId, crm, chefeId, analistaId, auditorId, ocsCnpj, cargo);
    }

    // Funcionário de OCS não tem nome no banco: mostra "Recepção · Clínica X" (igual ao mock do front).
    private String montarNomeDoFuncionario(FunOcsModel funcionario) {
        String ocs = funcionario.getOcsModel().getNome();
        if (funcionario.getCargo() == null || funcionario.getCargo().isBlank()) {
            return ocs;
        }
        return funcionario.getCargo() + " · " + ocs;
    }
}