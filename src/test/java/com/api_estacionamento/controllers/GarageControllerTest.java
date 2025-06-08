package com.api_estacionamento.controllers;


import com.api_estacionamento.contoller.GarageController;
import com.api_estacionamento.model.Vaga;
import com.api_estacionamento.service.GarageService;
import com.api_estacionamento.service.utils.ValidaHorarioUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GarageControllerTest {

    @InjectMocks
    private GarageController controller;

    @Mock
    private GarageService garageService;

    @Mock
    private ValidaHorarioUtils validaHorarioUtils;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Força a garagem a estar aberta
        ReflectionTestUtils.setField(controller, "garagemAberta", true);
    }

    @Test
    void deveAbrirEFecharGaragem() {
        // Primeiro toggle abre
        ReflectionTestUtils.setField(controller, "garagemAberta", false);
        String resposta1 = controller.alternarGaragem();
        assertEquals("Garagem aberta.", resposta1);

        // Segundo toggle fecha
        String resposta2 = controller.alternarGaragem();
        verify(garageService).liberarTodasAsVagas();
        assertEquals("Garagem fechada e todas as vagas foram liberadas.", resposta2);
    }

    @Test
    void deveRecusarEntradaQuandoGaragemFechada() {
        ReflectionTestUtils.setField(controller, "garagemAberta", false);

        String resposta = controller.entradaVeiculo(1L, LocalDateTime.now(), BigDecimal.TEN);
        assertEquals("Garagem está fechada.", resposta);
    }

    @Test
    void deveRecusarEntradaForaDoHorarioPermitido() {
        when(validaHorarioUtils.permitidoEntradaAgora()).thenReturn(false);

        String resposta = controller.entradaVeiculo(1L, LocalDateTime.now(), BigDecimal.TEN);
        assertEquals("A entrada não é permitida fora do horário autorizado pelas regras administrativas.", resposta);
    }

    @Test
    void devePermitirEntradaQuandoHorarioValido() {
        when(validaHorarioUtils.permitidoEntradaAgora()).thenReturn(true);
        when(garageService.entradaVeiculo(anyLong(), any(), any())).thenReturn("Entrada registrada com sucesso.");

        String resposta = controller.entradaVeiculo(1L, LocalDateTime.now(), BigDecimal.TEN);
        assertEquals("Entrada registrada com sucesso.", resposta);
    }

    @Test
    void deveRecusarSaidaQuandoGaragemFechada() {
        ReflectionTestUtils.setField(controller, "garagemAberta", false);

        String resposta = controller.saidaVeiculo(1L, LocalDateTime.now(), BigDecimal.TEN);
        assertEquals("Garagem está fechada.", resposta);
    }

    @Test
    void devePermitirSaidaQuandoGaragemAberta() {
        when(garageService.saidaVeiculo(anyLong(), any(), any())).thenReturn("Saída registrada com sucesso.");

        String resposta = controller.saidaVeiculo(1L, LocalDateTime.now(), BigDecimal.TEN);
        assertEquals("Saída registrada com sucesso.", resposta);
    }

    @Test
    void deveConsultarStatusVaga() {
        when(garageService.verificaVagaEmUso(1L)).thenReturn("Status da vaga: LIVRE");

        String resposta = controller.consultaVaga(1L);
        assertEquals("Status da vaga: LIVRE", resposta);
    }

    @Test
    void deveListarTodasAsVagas() {
        Vaga vaga1 = new Vaga();
        Vaga vaga2 = new Vaga();
        when(garageService.listarTodas()).thenReturn(List.of(vaga1, vaga2));

        List<Vaga> lista = controller.listarTodas();
        assertEquals(2, lista.size());
    }
}