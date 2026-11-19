package com.datasphere.fusex.espelho.service;

import com.datasphere.fusex.espelhoitem.EspelhoItemModel;
import org.springframework.stereotype.Service;

@Service
public class VincularItemAoBeneficiarioService {

    public EspelhoItemModel vincularItemAoBeneficiario(EspelhoItemModel item, String beneficiarioId) {
        item.setBeneficiarioId(beneficiarioId);
        return item;
    }
}