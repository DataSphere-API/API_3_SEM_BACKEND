package com.datasphere.fusex.preguia.service;

import com.datasphere.fusex.chefe.ChefeFusexRepository;
import com.datasphere.fusex.preguia.PreGuiaModel;
import org.springframework.stereotype.Service;

@Service
public class VincularChefeFusexService {

    private final ChefeFusexRepository chefeFusexRepository;

    public VincularChefeFusexService(ChefeFusexRepository chefeFusexRepository) {
        this.chefeFusexRepository = chefeFusexRepository;
    }


    public boolean vincularChefeFusex(PreGuiaModel preGuia, Long chefeId) {
        if (chefeId == null || !chefeFusexRepository.existsById(chefeId)) {
            return false;
        }
        preGuia.setChefeId(chefeId);
        return true;
    }
}