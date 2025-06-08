package com.api_estacionamento.dto;

import com.api_estacionamento.model.RegraAdministrativa;
import com.api_estacionamento.model.Vaga;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ImportsDTO {
    private List<Vaga> vagas;
    private List<RegraAdministrativa> regras;
}