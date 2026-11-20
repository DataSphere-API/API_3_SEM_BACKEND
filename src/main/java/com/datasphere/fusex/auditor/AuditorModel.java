package com.datasphere.fusex.auditor;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "auditor")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuditorModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "benef_id", length = 20, nullable = false)
    private String benefId;
}
