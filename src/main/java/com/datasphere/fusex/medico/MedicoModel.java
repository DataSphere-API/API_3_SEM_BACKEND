package com.datasphere.fusex.medico;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
    @Column(name = "id")
    private Integer id;

    @Column(name = "crm", length = 20, nullable = false)
    private String crm;
}
