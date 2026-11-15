package com.datasphere.fusex.auth.service;

import com.datasphere.fusex.auth.dto.CadastroRequest;
import com.datasphere.fusex.auth.dto.CadastroResponse;
import com.datasphere.fusex.beneficiario.BeneficiarioModel;
import com.datasphere.fusex.beneficiario.BeneficiarioRepository;
import com.datasphere.fusex.domain.enums.ErrorCode;
import com.datasphere.fusex.exception.BusinessException;
import com.datasphere.fusex.usuario.UsuarioModel;
import com.datasphere.fusex.usuario.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CadastrarBeneficiarioServiceTest {

    private static final LocalDate NASCIMENTO = LocalDate.of(1990, 1, 10);

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private BeneficiarioRepository beneficiarioRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    private CadastrarBeneficiarioService service;

    @BeforeEach
    void setUp() {
        service = new CadastrarBeneficiarioService(usuarioRepository, beneficiarioRepository,
                new ValidarPrecCpService(), passwordEncoder);
    }

    private CadastroRequest titular(String precCp) {
        return new CadastroRequest("Maria Silva", precCp, NASCIMENTO, false, null, "52998224725", "senhaForte1");
    }

    private CadastroRequest dependente(String precCp, String cpfResponsavel) {
        return new CadastroRequest("João Silva", precCp, NASCIMENTO, true, cpfResponsavel, "11144477735", "senhaForte1");
    }

    private BeneficiarioModel beneficiario(String precCp, Integer usuarioId, LocalDate nascimento) {
        BeneficiarioModel b = new BeneficiarioModel();
        b.setPrecCP(precCp);
        b.setNome("Já existente");
        b.setDataNasc(nascimento);
        b.setUsuarioId(usuarioId);
        return b;
    }

    private UsuarioModel usuario(Integer id) {
        UsuarioModel u = new UsuarioModel();
        u.setId(id);
        return u;
    }

    private void salvarUsuarioComId(int id) {
        when(passwordEncoder.encode("senhaForte1")).thenReturn("$2a$hash");
        when(usuarioRepository.save(any(UsuarioModel.class))).thenAnswer(chamada -> {
            UsuarioModel u = chamada.getArgument(0);
            u.setId(id);
            return u;
        });
    }

    @Test
    void titularNovo_criaBeneficiarioEUsuarioComSenhaEmHash() {
        salvarUsuarioComId(10);

        CadastroResponse resposta = service.cadastrar(titular("777777777-00"));

        ArgumentCaptor<UsuarioModel> usuario = ArgumentCaptor.forClass(UsuarioModel.class);
        verify(usuarioRepository).save(usuario.capture());
        assertEquals("52998224725", usuario.getValue().getLogin());
        assertEquals("$2a$hash", usuario.getValue().getSenha());

        ArgumentCaptor<BeneficiarioModel> beneficiario = ArgumentCaptor.forClass(BeneficiarioModel.class);
        verify(beneficiarioRepository).save(beneficiario.capture());
        assertEquals(10, beneficiario.getValue().getUsuarioId());
        assertEquals("Titular", beneficiario.getValue().getGrupo());
        assertEquals(new CadastroResponse(10, "Maria Silva", "777777777-00"), resposta);
    }

    @Test
    void precCpJaExisteSemLogin_soLigaOLogin() {
        BeneficiarioModel existente = beneficiario("111111111-00", null, NASCIMENTO);
        when(beneficiarioRepository.findByPrecCP("111111111-00")).thenReturn(Optional.of(existente));
        salvarUsuarioComId(11);

        CadastroResponse resposta = service.cadastrar(titular("111111111-00"));

        verify(beneficiarioRepository).save(existente);
        assertEquals(11, existente.getUsuarioId());
        assertEquals("Já existente", resposta.nome());
    }

    @Test
    void dependenteDoTitularCerto_eCriado() {
        when(usuarioRepository.findByLogin("52601815906")).thenReturn(Optional.of(usuario(6)));
        when(beneficiarioRepository.findByUsuarioId(6)).thenReturn(Optional.of(beneficiario("123456789-00", 6, NASCIMENTO)));
        salvarUsuarioComId(12);

        service.cadastrar(dependente("123456789-01", "526.018.159-06"));

        ArgumentCaptor<BeneficiarioModel> beneficiario = ArgumentCaptor.forClass(BeneficiarioModel.class);
        verify(beneficiarioRepository).save(beneficiario.capture());
        assertEquals("Dependente", beneficiario.getValue().getGrupo());
        assertTrue(beneficiario.getValue().isDependente());
    }

    @Test
    void precCpForaDoFormato_lancaPrecCpInvalido() {
        BusinessException ex = assertThrows(BusinessException.class, () -> service.cadastrar(titular("12345")));
        assertEquals(ErrorCode.PREC_CP_INVALIDO, ex.getErrorCode());
        verifyNoInteractions(usuarioRepository, beneficiarioRepository);
    }

    @Test
    void titularSemFinal00_lancaPrecCpTitularInvalido() {
        BusinessException ex = assertThrows(BusinessException.class, () -> service.cadastrar(titular("123456789-01")));
        assertEquals(ErrorCode.PREC_CP_TITULAR_INVALIDO, ex.getErrorCode());
    }

    @Test
    void dependenteComFinal00_lancaPrecCpDependenteInvalido() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.cadastrar(dependente("123456789-00", "52601815906")));
        assertEquals(ErrorCode.PREC_CP_DEPENDENTE_INVALIDO, ex.getErrorCode());
    }

    @Test
    void cpfDoResponsavelSemLogin_lancaResponsavelNaoEncontrado() {
        when(usuarioRepository.findByLogin("52601815906")).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.cadastrar(dependente("123456789-01", "52601815906")));
        assertEquals(ErrorCode.RESPONSAVEL_NAO_ENCONTRADO, ex.getErrorCode());
    }

    @Test
    void responsavelQueTambemEDependente_lancaResponsavelNaoEncontrado() {
        when(usuarioRepository.findByLogin("52601815906")).thenReturn(Optional.of(usuario(6)));
        when(beneficiarioRepository.findByUsuarioId(6)).thenReturn(Optional.of(beneficiario("123456789-02", 6, NASCIMENTO)));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.cadastrar(dependente("123456789-01", "52601815906")));
        assertEquals(ErrorCode.RESPONSAVEL_NAO_ENCONTRADO, ex.getErrorCode());
    }

    @Test
    void inicioDoPrecCpDiferenteDoTitular_lancaPrecCpNaoConfere() {
        when(usuarioRepository.findByLogin("52601815906")).thenReturn(Optional.of(usuario(6)));
        when(beneficiarioRepository.findByUsuarioId(6)).thenReturn(Optional.of(beneficiario("123456789-00", 6, NASCIMENTO)));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.cadastrar(dependente("987654321-01", "52601815906")));
        assertEquals(ErrorCode.PREC_CP_NAO_CONFERE, ex.getErrorCode());
    }

    @Test
    void precCpQueJaTemLogin_lancaBeneficiarioJaCadastrado() {
        when(beneficiarioRepository.findByPrecCP("123456789-00"))
                .thenReturn(Optional.of(beneficiario("123456789-00", 6, NASCIMENTO)));

        BusinessException ex = assertThrows(BusinessException.class, () -> service.cadastrar(titular("123456789-00")));
        assertEquals(ErrorCode.BENEFICIARIO_JA_CADASTRADO, ex.getErrorCode());
    }

    @Test
    void dataDeNascimentoDiferente_lancaBeneficiarioNaoConfere() {
        when(beneficiarioRepository.findByPrecCP("111111111-00"))
                .thenReturn(Optional.of(beneficiario("111111111-00", null, LocalDate.of(1979, 7, 22))));

        BusinessException ex = assertThrows(BusinessException.class, () -> service.cadastrar(titular("111111111-00")));
        assertEquals(ErrorCode.BENEFICIARIO_NAO_CONFERE, ex.getErrorCode());
    }

    @Test
    void cpfQueJaTemLogin_lancaCpfJaCadastrado() {
        when(usuarioRepository.existsByLogin("52998224725")).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.cadastrar(titular("777777777-00")));
        assertEquals(ErrorCode.CPF_JA_CADASTRADO, ex.getErrorCode());
        verify(usuarioRepository, never()).save(any());
        verify(beneficiarioRepository, never()).save(any());
    }
}