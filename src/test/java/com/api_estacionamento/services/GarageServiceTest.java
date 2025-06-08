package com.api_estacionamento.services;


import com.api_estacionamento.model.Lancamento;
import com.api_estacionamento.model.Vaga;
import com.api_estacionamento.model.utils.StatusVaga;
import com.api_estacionamento.repository.LancamentoRepository;
import com.api_estacionamento.repository.VagaRepository;
import com.api_estacionamento.service.GarageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class GarageServiceTest {

    @InjectMocks
    private GarageService service;

    @Mock
    private VagaRepository vagaRepository;

    @Mock
    private LancamentoRepository lancamentoRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void deveLiberarTodasAsVagas() {
        Vaga vaga1 = new Vaga(); vaga1.setStatus(StatusVaga.EM_USO);
        Vaga vaga2 = new Vaga(); vaga2.setStatus(StatusVaga.FECHADO);

        when(vagaRepository.findAll()).thenReturn(List.of(vaga1, vaga2));

        service.liberarTodasAsVagas();

        assertEquals(StatusVaga.LIVRE, vaga1.getStatus());
        assertEquals(StatusVaga.LIVRE, vaga2.getStatus());
        verify(vagaRepository).saveAll(List.of(vaga1, vaga2));
    }

    @Test
    void deveRegistrarEntradaComTarifaBaseSemAjuste() {
        Vaga vaga = new Vaga();
        vaga.setId_vaga(1L);
        vaga.setStatus(StatusVaga.LIVRE);
        vaga.setLancamentos(new ArrayList<>());

        when(vagaRepository.findById(1L)).thenReturn(Optional.of(vaga));
        when(vagaRepository.findAll()).thenReturn(List.of(vaga));

        String resposta = service.entradaVeiculo(1L, LocalDateTime.now(), BigDecimal.TEN);

        assertTrue(resposta.startsWith("Entrada registrada com sucesso."));
        assertEquals(StatusVaga.EM_USO, vaga.getStatus());
        verify(lancamentoRepository).save(any(Lancamento.class));
        verify(vagaRepository).save(vaga);
    }

    @Test
    void deveRecusarEntradaSeVagaNaoExiste() {
        when(vagaRepository.findById(anyLong())).thenReturn(Optional.empty());

        String resposta = service.entradaVeiculo(1L, LocalDateTime.now(), BigDecimal.TEN);

        assertEquals("Vaga não encontrada.", resposta);
    }

    @Test
    void deveRecusarEntradaSeVagaJaEmUso() {
        Vaga vaga = new Vaga();
        vaga.setStatus(StatusVaga.EM_USO);

        when(vagaRepository.findById(1L)).thenReturn(Optional.of(vaga));

        String resposta = service.entradaVeiculo(1L, LocalDateTime.now(), BigDecimal.TEN);

        assertEquals("Vaga já está em uso.", resposta);
    }

    @Test
    void deveRegistrarSaidaComSucesso() {
        Vaga vaga = new Vaga();
        vaga.setStatus(StatusVaga.EM_USO);

        Lancamento lancamento = new Lancamento();
        lancamento.setDataHoraEntrada(LocalDateTime.now().minusHours(2));
        lancamento.setDataHoraSaida(null);
        vaga.setLancamentos(new ArrayList<>(List.of(lancamento)));

        when(vagaRepository.findById(1L)).thenReturn(Optional.of(vaga));

        String resposta = service.saidaVeiculo(1L, LocalDateTime.now(), BigDecimal.valueOf(25));

        assertEquals("Saída registrada com sucesso.", resposta);
        assertEquals(StatusVaga.LIVRE, vaga.getStatus());
        assertNotNull(lancamento.getDataHoraSaida());
        assertEquals(BigDecimal.valueOf(25), lancamento.getValorPago());
        verify(lancamentoRepository).save(lancamento);
        verify(vagaRepository).save(vaga);
    }

    @Test
    void deveRecusarSaidaSeVagaNaoExiste() {
        when(vagaRepository.findById(1L)).thenReturn(Optional.empty());

        String resposta = service.saidaVeiculo(1L, LocalDateTime.now(), BigDecimal.TEN);
        assertEquals("Vaga não encontrada.", resposta);
    }

    @Test
    void deveRecusarSaidaSeVagaNaoEstiverEmUso() {
        Vaga vaga = new Vaga();
        vaga.setStatus(StatusVaga.LIVRE);

        when(vagaRepository.findById(1L)).thenReturn(Optional.of(vaga));

        String resposta = service.saidaVeiculo(1L, LocalDateTime.now(), BigDecimal.TEN);
        assertEquals("Vaga não está ocupada.", resposta);
    }

    @Test
    void deveRecusarSaidaSeNaoHouverLancamentoAberto() {
        Vaga vaga = new Vaga();
        vaga.setStatus(StatusVaga.EM_USO);
        Lancamento lancamento = new Lancamento();
        lancamento.setDataHoraSaida(LocalDateTime.now()); // já saiu
        vaga.setLancamentos(List.of(lancamento));

        when(vagaRepository.findById(1L)).thenReturn(Optional.of(vaga));

        String resposta = service.saidaVeiculo(1L, LocalDateTime.now(), BigDecimal.TEN);
        assertEquals("Nenhuma entrada registrada para essa vaga.", resposta);
    }

    @Test
    void deveRetornarStatusDaVaga() {
        Vaga vaga = new Vaga();
        vaga.setStatus(StatusVaga.FECHADO);

        when(vagaRepository.findById(1L)).thenReturn(Optional.of(vaga));

        String resposta = service.verificaVagaEmUso(1L);
        assertEquals("Status da vaga: FECHADO", resposta);
    }

    @Test
    void deveRetornarVagaNaoEncontradaNaConsulta() {
        when(vagaRepository.findById(1L)).thenReturn(Optional.empty());

        String resposta = service.verificaVagaEmUso(1L);
        assertEquals("Vaga não encontrada.", resposta);
    }

    @Test
    void deveListarTodasAsVagas() {
        Vaga v1 = new Vaga(); Vaga v2 = new Vaga();
        when(vagaRepository.findAll()).thenReturn(List.of(v1, v2));

        List<Vaga> vagas = service.listarTodas();
        assertEquals(2, vagas.size());
    }

    @Test
    void deveAplicarDescontoDe10PorcentoQuandoLotacaoMenorQue25() {
        Vaga vagaLivre = new Vaga(); vagaLivre.setStatus(StatusVaga.LIVRE);

        when(vagaRepository.findAll()).thenReturn(List.of(vagaLivre, vagaLivre, vagaLivre, vagaLivre));

        BigDecimal result = ReflectionTestUtils.invokeMethod(service,
                "calcularTarifaComBaseNaLotacao", BigDecimal.valueOf(100));

        assertEquals(BigDecimal.valueOf(90.00), result);
    }

    @Test
    void deveManterTarifaBaseQuandoLotacaoEntre25e50() {
        Vaga vaga1 = new Vaga(); vaga1.setStatus(StatusVaga.EM_USO);
        Vaga vaga2 = new Vaga(); vaga2.setStatus(StatusVaga.LIVRE);
        Vaga vaga3 = new Vaga(); vaga3.setStatus(StatusVaga.LIVRE);
        Vaga vaga4 = new Vaga(); vaga4.setStatus(StatusVaga.LIVRE);

        when(vagaRepository.findAll()).thenReturn(List.of(vaga1, vaga2, vaga3, vaga4));

        BigDecimal result = ReflectionTestUtils.invokeMethod(service,
                "calcularTarifaComBaseNaLotacao", BigDecimal.valueOf(100));

        assertEquals(BigDecimal.valueOf(100), result);
    }

    @Test
    void deveAplicarAumentoDe10PorcentoQuandoLotacaoAte75() {
        Vaga vaga1 = new Vaga(); vaga1.setStatus(StatusVaga.EM_USO);
        Vaga vaga2 = new Vaga(); vaga2.setStatus(StatusVaga.EM_USO);
        Vaga vaga3 = new Vaga(); vaga3.setStatus(StatusVaga.EM_USO);
        Vaga vaga4 = new Vaga(); vaga4.setStatus(StatusVaga.LIVRE);

        when(vagaRepository.findAll()).thenReturn(List.of(vaga1, vaga2, vaga3, vaga4));

        BigDecimal result = ReflectionTestUtils.invokeMethod(service,
                "calcularTarifaComBaseNaLotacao", BigDecimal.valueOf(100));

        assertEquals(BigDecimal.valueOf(110.00), result);
    }

    @Test
    void deveAplicarAumentoDe25PorcentoQuandoLotacaoAcimaDe75() {
        Vaga vaga1 = new Vaga(); vaga1.setStatus(StatusVaga.EM_USO);
        Vaga vaga2 = new Vaga(); vaga2.setStatus(StatusVaga.EM_USO);
        Vaga vaga3 = new Vaga(); vaga3.setStatus(StatusVaga.EM_USO);
        Vaga vaga4 = new Vaga(); vaga4.setStatus(StatusVaga.EM_USO);

        when(vagaRepository.findAll()).thenReturn(List.of(vaga1, vaga2, vaga3, vaga4));

        BigDecimal result = ReflectionTestUtils.invokeMethod(service,
                "calcularTarifaComBaseNaLotacao", BigDecimal.valueOf(100));

        assertEquals(0, result.compareTo(BigDecimal.valueOf(125)));
    }
}