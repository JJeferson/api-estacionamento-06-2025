package com.api_estacionamento.service;

import com.api_estacionamento.model.RegraAdministrativa;
import com.api_estacionamento.repository.RegraAdministrativaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RegraAdministrativaService {

    @Autowired
    private RegraAdministrativaRepository regraRepository;

    public List<RegraAdministrativa> listarTodas() {
        return regraRepository.findAll();
    }

    public Optional<RegraAdministrativa> buscarPorId(Long id) {
        return regraRepository.findById(id);
    }

    public RegraAdministrativa salvar(RegraAdministrativa regra) {
        return regraRepository.save(regra);
    }

    public void deletar(Long id) {
        regraRepository.deleteById(id);
    }
}
