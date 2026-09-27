package com.datasphere.fusex.ocs;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ocs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OcsModel {

    @Id
    @Column(name = "cnpj", length = 14, nullable = false)
    private String cnpj;

    @Column(name = "nome", length = 150, nullable = false)
    private String nome;
}