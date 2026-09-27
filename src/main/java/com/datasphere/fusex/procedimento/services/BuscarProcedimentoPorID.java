package com.datasphere.fusex.procedimento.services;


import com.datasphere.fusex.procedimento.ProcedimentoModel;
import com.datasphere.fusex.procedimento.ProcedimentoRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BuscarProcedimentoPorID {
    private final ProcedimentoRepository procedimentoRepository;

    public  BuscarProcedimentoPorID(ProcedimentoRepository procedimentoRepository){
        this.procedimentoRepository = procedimentoRepository;
    }

    public ProcedimentoModel buscarProcedimentoPorID(Integer id){
        Optional<ProcedimentoModel> procedimentoModelOptional = procedimentoRepository.findById(id);
        return  procedimentoModelOptional.orElse(null);
    }
}
