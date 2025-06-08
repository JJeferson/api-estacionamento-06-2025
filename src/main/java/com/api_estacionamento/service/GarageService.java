package com.api_estacionamento.service;

import com.api_estacionamento.model.Lancamento;
import com.api_estacionamento.model.Vaga;
import com.api_estacionamento.model.utils.StatusVaga;
import com.api_estacionamento.repository.LancamentoRepository;
import com.api_estacionamento.repository.VagaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class GarageService {

    @Autowired
    private VagaRepository vagaRepository;

    @Autowired
    private LancamentoRepository lancamentoRepository;

    public void liberarTodasAsVagas() {
        List<Vaga> vagas = vagaRepository.findAll();
        for (Vaga vaga : vagas) {
            vaga.setStatus(StatusVaga.LIVRE);
        }
        vagaRepository.saveAll(vagas);
    }

    public String entradaVeiculo(Long idVaga, LocalDateTime entrada, BigDecimal tarifaBase) {
        // 1. Verifica se TODAS estão ocupadas
        List<Vaga> vagas = vagaRepository.findAll();
        boolean todasEmUso = vagas.stream().allMatch(v -> v.getStatus() == StatusVaga.EM_USO);
        if (todasEmUso) {
            throw new RuntimeException("Não existe uma vaga disponivel");
        }

        // 2. Agora verifica se a vaga existe
        Optional<Vaga> optionalVaga = vagaRepository.findById(idVaga);
        if (optionalVaga.isEmpty()) {
            throw new RuntimeException("Vaga não encontrada.");
        }

        // 3. Verifica se a vaga específica está em uso
        Vaga vaga = optionalVaga.get();
        if (vaga.getStatus() == StatusVaga.EM_USO) {
            throw new RuntimeException("Essa vaga esta em uso");
        }

        // 4. Tudo OK, segue o fluxo
        BigDecimal tarifaFinal = calcularTarifaComBaseNaLotacao(tarifaBase);

        Lancamento lancamento = new Lancamento();
        lancamento.setStatus(StatusVaga.EM_USO);
        lancamento.setDataHoraEntrada(entrada);
        lancamento.setValorTarifa(tarifaFinal);
        lancamento.setVaga(vaga);

        vaga.setStatus(StatusVaga.EM_USO);
        vaga.getLancamentos().add(lancamento);

        lancamentoRepository.save(lancamento);
        vagaRepository.save(vaga);

        return "Entrada registrada com sucesso. Tarifa aplicada: R$ " + tarifaFinal;
    }



    public String saidaVeiculo(Long idVaga, LocalDateTime saida, BigDecimal valorPago) {
        Optional<Vaga> optionalVaga = vagaRepository.findById(idVaga);
        if (optionalVaga.isEmpty()) return "Vaga não encontrada.";

        Vaga vaga = optionalVaga.get();
        if (vaga.getStatus() != StatusVaga.EM_USO) return "Vaga não está ocupada.";

        Lancamento ultimoLancamento = vaga.getLancamentos().stream()
                .filter(l -> l.getDataHoraSaida() == null)
                .reduce((first, second) -> second).orElse(null);

        if (ultimoLancamento == null) return "Nenhuma entrada registrada para essa vaga.";

        ultimoLancamento.setDataHoraSaida(saida);
        ultimoLancamento.setValorPago(valorPago);
        ultimoLancamento.setStatus(StatusVaga.LIVRE);
        ultimoLancamento.setTempoDePermanencia(Duration.between(ultimoLancamento.getDataHoraEntrada(), saida));

        vaga.setStatus(StatusVaga.LIVRE);

        lancamentoRepository.save(ultimoLancamento);
        vagaRepository.save(vaga);

        return "Saída registrada com sucesso.";
    }

    public String verificaVagaEmUso(Long idVaga) {
        Optional<Vaga> optionalVaga = vagaRepository.findById(idVaga);
        if (optionalVaga.isEmpty()) return "Vaga não encontrada.";

        Vaga vaga = optionalVaga.get();
        return "Status da vaga: " + vaga.getStatus();
    }

    public List<Vaga> listarTodas() {
        return vagaRepository.findAll();
    }

    private BigDecimal calcularTarifaComBaseNaLotacao(BigDecimal tarifaBase) {
        List<Vaga> todas = vagaRepository.findAll();
        if (todas.isEmpty()) return tarifaBase;

        long emUso = todas.stream().filter(v -> v.getStatus() == StatusVaga.EM_USO).count();
        double lotacao = (double) emUso / todas.size();

        if (lotacao < 0.25) {
            return tarifaBase.multiply(BigDecimal.valueOf(0.90));
        } else if (lotacao <= 0.50) {
            return tarifaBase;
        } else if (lotacao <= 0.75) {
            return tarifaBase.multiply(BigDecimal.valueOf(1.10));
        } else {
            return tarifaBase.multiply(BigDecimal.valueOf(1.25));
        }
    }
}