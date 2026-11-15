package com.datasphere.fusex.auth.service;

import com.datasphere.fusex.analista.AnalistaRepository;
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
import com.datasphere.fusex.ocs.OcsModel;
import com.datasphere.fusex.usuario.UsuarioModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.EnumSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MontarUsuarioLogadoServiceTest {

    // Métodos que devolvem Optional e não foram configurados devolvem Optional.empty().
    @Mock
    private BeneficiarioRepository beneficiarioRepository;
    @Mock
    private MedicoRepository medicoRepository;
    @Mock
    private ChefeFusexRepository chefeFusexRepository;
    @Mock
    private AnalistaRepository analistaRepository;
    @Mock
    private AuditorRepository auditorRepository;
    @Mock
    private FunOcsRepository funOcsRepository;

    @InjectMocks
    private MontarUsuarioLogadoService service;

    private UsuarioModel usuario(int id, String cpf) {
        UsuarioModel u = new UsuarioModel();
        u.setId(id);
        u.setLogin(cpf);
        return u;
    }

    private BeneficiarioModel beneficiario(String precCp, String nome) {
        BeneficiarioModel b = new BeneficiarioModel();
        b.setPrecCP(precCp);
        b.setNome(nome);
        return b;
    }

    @Test
    void medicoTambemEBeneficiario() {
        MedicoModel medico = new MedicoModel();
        medico.setId(1);
        medico.setCrm("SP123456");
        when(beneficiarioRepository.findByUsuarioId(2)).thenReturn(Optional.of(beneficiario("222222222-00", "Carlos Medeiros")));
        when(medicoRepository.findByBenefId("222222222-00")).thenReturn(Optional.of(medico));

        UsuarioLogado logado = service.montar(usuario(2, "08301661305"));

        assertEquals(EnumSet.of(Perfil.MEDICO, Perfil.BENEFICIARIO), logado.perfis());
        assertEquals("Carlos Medeiros", logado.nome());
        assertEquals(1, logado.medicoId());
        assertEquals("SP123456", logado.crm());
        assertEquals("222222222-00", logado.precCp());
        assertNull(logado.ocsCnpj());
    }

    @Test
    void chefeRecebeOIdDoChefe() {
        ChefeFusexModel chefe = new ChefeFusexModel();
        chefe.setId(1L);
        when(beneficiarioRepository.findByUsuarioId(1)).thenReturn(Optional.of(beneficiario("444444444-00", "Roberto Lima")));
        when(chefeFusexRepository.findByBenefId("444444444-00")).thenReturn(Optional.of(chefe));

        UsuarioLogado logado = service.montar(usuario(1, "52601815906"));

        assertEquals(EnumSet.of(Perfil.CHEFE, Perfil.BENEFICIARIO), logado.perfis());
        assertEquals(1, logado.chefeId());
    }

    @Test
    void funcionarioDeOcs_temSoPerfilOcsENomeComCargo() {
        FunOcsModel funcionario = new FunOcsModel(1, "Recepção", new OcsModel("12345678000199", "Clínica X"), 5);
        when(funOcsRepository.findByUsuarioId(5)).thenReturn(Optional.of(funcionario));

        UsuarioLogado logado = service.montar(usuario(5, "62819482112"));

        assertEquals(EnumSet.of(Perfil.OCS), logado.perfis());
        assertEquals("Recepção · Clínica X", logado.nome());
        assertEquals("12345678000199", logado.ocsCnpj());
        assertNull(logado.precCp());
    }

    @Test
    void usuarioSemNenhumaLigacao_ficaSemPerfilEComCpfNoNome() {
        UsuarioLogado logado = service.montar(usuario(9, "11144477735"));

        assertEquals(EnumSet.noneOf(Perfil.class), logado.perfis());
        assertEquals("11144477735", logado.nome());
    }
}