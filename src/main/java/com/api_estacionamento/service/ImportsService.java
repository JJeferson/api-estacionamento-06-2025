package com.api_estacionamento.service;


import com.api_estacionamento.dto.ImportsDTO;
import com.api_estacionamento.model.RegraAdministrativa;
import com.api_estacionamento.model.Vaga;
import com.api_estacionamento.repository.RegraAdministrativaRepository;
import com.api_estacionamento.repository.VagaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ImportsService {

    @Autowired
    private VagaRepository vagaRepository;

    @Autowired
    private RegraAdministrativaRepository regraRepository;

    public void importar(ImportsDTO dto) {
        List<Vaga> vagas = dto.getVagas();
        List<RegraAdministrativa> regras = dto.getRegras();

        if (vagas != null && !vagas.isEmpty()) {
            vagaRepository.saveAll(vagas);
        }

        if (regras != null && !regras.isEmpty()) {
            regraRepository.saveAll(regras);
        }
    }
}
