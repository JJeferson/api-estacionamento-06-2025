package com.api_estacionamento.model;


import com.api_estacionamento.model.utils.StatusVaga;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "lancamento")
public class Lancamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private StatusVaga status;

    private LocalDateTime dataHoraEntrada;

    private LocalDateTime dataHoraSaida;

    private BigDecimal valorTarifa;

    private BigDecimal valorPago;

    private Duration tempoDePermanencia;

    @ManyToOne
    @JoinColumn(name = "vaga_id")
    private Vaga vaga;
}