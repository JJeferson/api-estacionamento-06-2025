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
    void deveLancarErroQuandoTodasVagasEstiveremEmUso() {
        // Vaga solicitada existe e está livre (mas simula que todas estão ocupadas)
        Vaga vagaSolicitada = new Vaga();
        vagaSolicitada.setId_vaga(1L);
        vagaSolicitada.setStatus(StatusVaga.LIVRE);
        vagaSolicitada.setLancamentos(new ArrayList<>());

        // Todas as outras vagas em uso
        Vaga vaga2 = new Vaga(); vaga2.setStatus(StatusVaga.EM_USO);
        Vaga vaga3 = new Vaga(); vaga3.setStatus(StatusVaga.EM_USO);

        // Retorna a vaga solicitada no findById
        when(vagaRepository.findById(1L)).thenReturn(Optional.of(vagaSolicitada));

        // Simula que todas as vagas do sistema estão em uso (inclusive a solicitada)
        when(vagaRepository.findAll()).thenReturn(List.of(
                new Vaga() {{ setStatus(StatusVaga.EM_USO); }},
                new Vaga() {{ setStatus(StatusVaga.EM_USO); }},
                new Vaga() {{ setStatus(StatusVaga.EM_USO); }}
        ));

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                service.entradaVeiculo(1L, LocalDateTime.now(), BigDecimal.TEN)
        );

        assertEquals("Não existe uma vaga disponivel", ex.getMessage());
    }


    @Test
    void deveLancarErroQuandoVagaNaoExiste() {
        Vaga v1 = new Vaga(); v1.setStatus(StatusVaga.LIVRE);
        when(vagaRepository.findAll()).thenReturn(List.of(v1));
        when(vagaRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                service.entradaVeiculo(1L, LocalDateTime.now(), BigDecimal.TEN)
        );
        assertEquals("Vaga não encontrada.", ex.getMessage());
    }

    @Test
    void deveLancarErroQuandoVagaEstaEmUso() {
        Vaga vaga = new Vaga(); vaga.setStatus(StatusVaga.EM_USO);
        when(vagaRepository.findById(1L)).thenReturn(Optional.of(vaga));
        when(vagaRepository.findAll()).thenReturn(List.of(vaga));

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                service.entradaVeiculo(1L, LocalDateTime.now(), BigDecimal.TEN)
        );
    }

    @Test
    void deveRegistrarSaidaComSucesso() {
        Vaga vaga = new Vaga(); vaga.setStatus(StatusVaga.EM_USO);
        Lancamento lanc = new Lancamento();
        lanc.setDataHoraEntrada(LocalDateTime.now().minusHours(1));
        lanc.setDataHoraSaida(null);
        vaga.setLancamentos(List.of(lanc));
        when(vagaRepository.findById(1L)).thenReturn(Optional.of(vaga));

        String res = service.saidaVeiculo(1L, LocalDateTime.now(), BigDecimal.TEN);
        assertEquals("Saída registrada com sucesso.", res);
        assertEquals(StatusVaga.LIVRE, vaga.getStatus());
        verify(lancamentoRepository).save(lanc);
        verify(vagaRepository).save(vaga);
    }

    @Test
    void deveRecusarSaidaSeVagaNaoExiste() {
        when(vagaRepository.findById(1L)).thenReturn(Optional.empty());
        String res = service.saidaVeiculo(1L, LocalDateTime.now(), BigDecimal.TEN);
        assertEquals("Vaga não encontrada.", res);
    }

    @Test
    void deveRecusarSaidaSeVagaNaoEstiverEmUso() {
        Vaga vaga = new Vaga(); vaga.setStatus(StatusVaga.LIVRE);
        when(vagaRepository.findById(1L)).thenReturn(Optional.of(vaga));
        String res = service.saidaVeiculo(1L, LocalDateTime.now(), BigDecimal.TEN);
        assertEquals("Vaga não está ocupada.", res);
    }

    @Test
    void deveRecusarSaidaSeNaoHouverLancamentoAberto() {
        Vaga vaga = new Vaga(); vaga.setStatus(StatusVaga.EM_USO);
        Lancamento lanc = new Lancamento(); lanc.setDataHoraSaida(LocalDateTime.now());
        vaga.setLancamentos(List.of(lanc));
        when(vagaRepository.findById(1L)).thenReturn(Optional.of(vaga));
        String res = service.saidaVeiculo(1L, LocalDateTime.now(), BigDecimal.TEN);
        assertEquals("Nenhuma entrada registrada para essa vaga.", res);
    }

    @Test
    void deveRetornarStatusDaVaga() {
        Vaga vaga = new Vaga(); vaga.setStatus(StatusVaga.FECHADO);
        when(vagaRepository.findById(1L)).thenReturn(Optional.of(vaga));
        String res = service.verificaVagaEmUso(1L);
        assertEquals("Status da vaga: FECHADO", res);
    }

    @Test
    void deveRetornarVagaNaoEncontradaNaConsulta() {
        when(vagaRepository.findById(1L)).thenReturn(Optional.empty());
        String res = service.verificaVagaEmUso(1L);
        assertEquals("Vaga não encontrada.", res);
    }

    @Test
    void deveListarTodasAsVagas() {
        when(vagaRepository.findAll()).thenReturn(List.of(new Vaga(), new Vaga()));
        assertEquals(2, service.listarTodas().size());
    }

    @Test
    void deveAplicarDescontoDe10PorcentoQuandoLotacaoMenorQue25() {
        Vaga vaga = new Vaga(); vaga.setStatus(StatusVaga.LIVRE);
        when(vagaRepository.findAll()).thenReturn(List.of(vaga, vaga, vaga, vaga));

        BigDecimal result = ReflectionTestUtils.invokeMethod(service, "calcularTarifaComBaseNaLotacao", BigDecimal.valueOf(100));
        assertEquals(BigDecimal.valueOf(90.00), result);
    }

    @Test
    void deveManterTarifaBaseQuandoLotacaoEntre25e50() {
        Vaga v1 = new Vaga(); v1.setStatus(StatusVaga.EM_USO);
        Vaga v2 = new Vaga(); v2.setStatus(StatusVaga.LIVRE);
        Vaga v3 = new Vaga(); v3.setStatus(StatusVaga.LIVRE);
        Vaga v4 = new Vaga(); v4.setStatus(StatusVaga.LIVRE);
        when(vagaRepository.findAll()).thenReturn(List.of(v1, v2, v3, v4));

        BigDecimal result = ReflectionTestUtils.invokeMethod(service, "calcularTarifaComBaseNaLotacao", BigDecimal.valueOf(100));
        assertEquals(BigDecimal.valueOf(100), result);
    }

    @Test
    void deveAplicarAumentoDe10PorcentoQuandoLotacaoAte75() {
        Vaga v1 = new Vaga(); v1.setStatus(StatusVaga.EM_USO);
        Vaga v2 = new Vaga(); v2.setStatus(StatusVaga.EM_USO);
        Vaga v3 = new Vaga(); v3.setStatus(StatusVaga.EM_USO);
        Vaga v4 = new Vaga(); v4.setStatus(StatusVaga.LIVRE);
        when(vagaRepository.findAll()).thenReturn(List.of(v1, v2, v3, v4));

        BigDecimal result = ReflectionTestUtils.invokeMethod(service, "calcularTarifaComBaseNaLotacao", BigDecimal.valueOf(100));
        assertEquals(BigDecimal.valueOf(110.00), result);
    }

    @Test
    void deveAplicarAumentoDe25PorcentoQuandoLotacaoAcimaDe75() {
        Vaga v1 = new Vaga(); v1.setStatus(StatusVaga.EM_USO);
        Vaga v2 = new Vaga(); v2.setStatus(StatusVaga.EM_USO);
        Vaga v3 = new Vaga(); v3.setStatus(StatusVaga.EM_USO);
        Vaga v4 = new Vaga(); v4.setStatus(StatusVaga.EM_USO);
        when(vagaRepository.findAll()).thenReturn(List.of(v1, v2, v3, v4));

        BigDecimal result = ReflectionTestUtils.invokeMethod(service, "calcularTarifaComBaseNaLotacao", BigDecimal.valueOf(100));
        assertEquals(0, result.compareTo(BigDecimal.valueOf(125)));

    }

    @Test
    void deveLancarErroQuandoTodasAsVagasEstaoEmUsoMesmoComVagaLivreSolicitada() {
        Vaga vagaSolicitada = new Vaga();
        vagaSolicitada.setId_vaga(1L);
        vagaSolicitada.setStatus(StatusVaga.LIVRE);
        vagaSolicitada.setLancamentos(new ArrayList<>());

        Vaga vaga2 = new Vaga(); vaga2.setStatus(StatusVaga.EM_USO);
        Vaga vaga3 = new Vaga(); vaga3.setStatus(StatusVaga.EM_USO);

        when(vagaRepository.findById(1L)).thenReturn(Optional.of(vagaSolicitada));
        when(vagaRepository.findAll()).thenReturn(List.of(vagaSolicitada, vaga2, vaga3));

        // Neste caso, 2 de 3 vagas estão em uso — NÃO é 100%, então não deve dar erro ainda.
        // Vamos simular o cenário com 100% ocupadas:

        vagaSolicitada.setStatus(StatusVaga.LIVRE); // momentaneamente livre
        when(vagaRepository.findAll()).thenReturn(List.of(
                vaga2, vaga3, new Vaga() {{
                    setStatus(StatusVaga.EM_USO);
                }}
        ));

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                service.entradaVeiculo(1L, LocalDateTime.now(), BigDecimal.TEN)
        );

        assertEquals("Não existe uma vaga disponivel", ex.getMessage());
    }

}