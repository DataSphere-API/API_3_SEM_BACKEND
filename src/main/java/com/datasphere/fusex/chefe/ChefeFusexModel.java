package com.datasphere.fusex.chefe;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "chefe_fusex")
@Getter
@Setter
@NoArgsConstructor
public class ChefeFusexModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "benef_id", nullable = false, length = 20)
    private String benefId;
}