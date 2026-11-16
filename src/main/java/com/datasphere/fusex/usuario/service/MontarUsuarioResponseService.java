package com.datasphere.fusex.usuario.service;

import com.datasphere.fusex.analista.AnalistaRepository;
import com.datasphere.fusex.auditor.AuditorRepository;
import com.datasphere.fusex.beneficiario.BeneficiarioRepository;
import com.datasphere.fusex.chefe.ChefeFusexRepository;
import com.datasphere.fusex.medico.MedicoRepository;
import com.datasphere.fusex.ocs.FunOcsRepository;
import com.datasphere.fusex.usuario.UsuarioModel;
import com.datasphere.fusex.usuario.dto.UsuarioResponse;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class MontarUsuarioResponseService {

    private final BeneficiarioRepository beneficiarioRepository;
    private final FunOcsRepository funOcsRepository;
    private final ChefeFusexRepository chefeRepository;
    private final MedicoRepository medicoRepository;
    private final AnalistaRepository analistaRepository;
    private final AuditorRepository auditorRepository;

    public MontarUsuarioResponseService(BeneficiarioRepository beneficiarioRepository,
                                        FunOcsRepository funOcsRepository, ChefeFusexRepository chefeRepository,
                                        MedicoRepository medicoRepository, AnalistaRepository analistaRepository,
                                        AuditorRepository auditorRepository) {
        this.beneficiarioRepository = beneficiarioRepository;
        this.funOcsRepository = funOcsRepository;
        this.chefeRepository = chefeRepository;
        this.medicoRepository = medicoRepository;
        this.analistaRepository = analistaRepository;
        this.auditorRepository = auditorRepository;
    }

    public UsuarioResponse montar(UsuarioModel usuario) {
        var beneficiario = beneficiarioRepository.findByUsuarioId(usuario.getId()).orElse(null);
        if (beneficiario != null) {
            String precCp = beneficiario.getPrecCP();
            List<String> perfis = new ArrayList<>();
            perfis.add("BENEFICIARIO");
            if (chefeRepository.findByBenefId(precCp).isPresent()) perfis.add("CHEFE");
            var medico = medicoRepository.findByBenefId(precCp).orElse(null);
            if (medico != null) perfis.add("MEDICO");
            if (analistaRepository.findByBenefId(precCp).isPresent()) perfis.add("EMISSOR");
            if (auditorRepository.findByBenefId(precCp).isPresent()) perfis.add("AUDITOR");
            return new UsuarioResponse(usuario.getId(), usuario.getLogin(), beneficiario.getNome(),
                    List.copyOf(perfis), precCp, medico == null ? null : medico.getCrm(), null, null);
        }
        var funcionario = funOcsRepository.findByUsuarioId(usuario.getId()).orElse(null);
        if (funcionario != null) {
            String cargo = funcionario.getCargo();
            String nome = (cargo == null || cargo.isBlank() ? "" : cargo + " · ")
                    + funcionario.getOcsModel().getNome();
            return new UsuarioResponse(usuario.getId(), usuario.getLogin(), nome, List.of("OCS"),
                    null, null, funcionario.getOcsModel().getCnpj(), cargo);
        }
        return new UsuarioResponse(usuario.getId(), usuario.getLogin(), usuario.getLogin(),
                List.of(), null, null, null, null);
    }
}
