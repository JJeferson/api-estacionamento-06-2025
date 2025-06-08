package com.api_estacionamento.service.utils;

import com.api_estacionamento.model.RegraAdministrativa;
import com.api_estacionamento.model.utils.StatusRegra;
import com.api_estacionamento.repository.RegraAdministrativaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Time;
import java.time.LocalTime;
import java.util.List;

@Component
public class ValidaHorarioUtils {

    @Autowired
    private RegraAdministrativaRepository regraRepository;

    public boolean permitidoEntradaAgora() {
        List<RegraAdministrativa> regras = regraRepository.findAll();
        LocalTime agora = LocalTime.now();

        return regras.stream()
                .filter(r -> r.getStatus() == StatusRegra.Ativa)
                .anyMatch(r -> {
                    Time entrada = r.getHoraEntrada();
                    Time saida = r.getHoraSaida();
                    return entrada != null && saida != null &&
                            !agora.isBefore(entrada.toLocalTime()) &&
                            !agora.isAfter(saida.toLocalTime());
                });
    }
}
