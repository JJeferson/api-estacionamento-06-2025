package com.api_estacionamento.model;

import com.api_estacionamento.model.utils.StatusRegra;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Time;

@Getter
@Setter
@Entity
@Table(name = "regra_administrativa ")
public class RegraAdministrativa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_regra;

    @Enumerated(EnumType.STRING)
    private StatusRegra status;

    private Time horaEntrada;
    private Time horaSaida;

}
