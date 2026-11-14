package com.datasphere.fusex.medico.service;

import com.datasphere.fusex.medico.MedicoRepository;
import com.datasphere.fusex.medico.dto.MedicosResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListarMedicosService {

    private final MedicoRepository medicoRepository;

    public ListarMedicosService(MedicoRepository medicoRepository) {
        this.medicoRepository = medicoRepository;
    }

    @Transactional(readOnly = true)
    public MedicosResponse listar() {
        return new MedicosResponse(medicoRepository.listarComNome());
    }
}
