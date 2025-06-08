package com.api_estacionamento.model;

import com.api_estacionamento.model.utils.StatusVaga;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
@Entity
@Table(name = "vaga")
public class Vaga {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_vaga;

    @Enumerated(EnumType.STRING)
    private StatusVaga status;

    private String geolocalizacao;


    @OneToMany(mappedBy = "vaga", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Lancamento> lancamentos;
}
