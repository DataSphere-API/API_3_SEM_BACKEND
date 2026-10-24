package com.datasphere.fusex.medico;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "medico")
@Getter
@Setter
@NoArgsConstructor
public class MedicoModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "crm", nullable = false, length = 20)
    private String crm;

    @Column(name = "benef_id", nullable = false, length = 20)
    private String benefId;
}