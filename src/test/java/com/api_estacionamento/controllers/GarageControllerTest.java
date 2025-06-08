package com.api_estacionamento.controllers;


import com.api_estacionamento.contoller.GarageController;
import com.api_estacionamento.model.Vaga;
import com.api_estacionamento.service.GarageService;
import com.api_estacionamento.service.utils.ValidaHorarioUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;
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
        ReflectionTestUtils.setField(controller, "garagemAberta", true);
    }

    @Test
    void deveAbrirEFecharGaragem() {
        ReflectionTestUtils.setField(controller, "garagemAberta", false);
        String aberta = controller.alternarGaragem();
        assertEquals("Garagem aberta.", aberta);

        String fechada = controller.alternarGaragem();
        verify(garageService).liberarTodasAsVagas();
        assertEquals("Garagem fechada e todas as vagas foram liberadas.", fechada);
    }

    @Test
    void deveRecusarEntradaComGaragemFechada() {
        ReflectionTestUtils.setField(controller, "garagemAberta", false);
        ResponseEntity<String> resp = controller.entradaVeiculo(1L, LocalDateTime.now(), BigDecimal.TEN);
        assertEquals(400, resp.getStatusCodeValue());
        assertEquals("Garagem está fechada.", resp.getBody());
    }

    @Test
    void deveRecusarEntradaForaDoHorario() {
        when(validaHorarioUtils.permitidoEntradaAgora()).thenReturn(false);
        ResponseEntity<String> resp = controller.entradaVeiculo(1L, LocalDateTime.now(), BigDecimal.TEN);
        assertEquals(400, resp.getStatusCodeValue());
        assertEquals("A entrada não é permitida fora do horário autorizado pelas regras administrativas.", resp.getBody());
    }

    @Test
    void deveTratarErroEmEntradaComMensagem500() {
        when(validaHorarioUtils.permitidoEntradaAgora()).thenReturn(true);
        when(garageService.entradaVeiculo(anyLong(), any(), any()))
                .thenThrow(new RuntimeException("Não existe uma vaga disponivel"));

        ResponseEntity<String> resp = controller.entradaVeiculo(1L, LocalDateTime.now(), BigDecimal.TEN);
        assertEquals(500, resp.getStatusCodeValue());
        assertEquals("Não existe uma vaga disponivel", resp.getBody());
    }

    @Test
    void devePermitirEntradaValida() {
        when(validaHorarioUtils.permitidoEntradaAgora()).thenReturn(true);
        when(garageService.entradaVeiculo(anyLong(), any(), any()))
                .thenReturn("Entrada registrada com sucesso.");
        ResponseEntity<String> resp = controller.entradaVeiculo(1L, LocalDateTime.now(), BigDecimal.TEN);
        assertEquals(200, resp.getStatusCodeValue());
        assertEquals("Entrada registrada com sucesso.", resp.getBody());
    }

    @Test
    void deveRecusarSaidaComGaragemFechada() {
        ReflectionTestUtils.setField(controller, "garagemAberta", false);
        String resposta = controller.saidaVeiculo(1L, LocalDateTime.now(), BigDecimal.TEN);
        assertEquals("Garagem está fechada.", resposta);
    }

    @Test
    void devePermitirSaida() {
        when(garageService.saidaVeiculo(anyLong(), any(), any())).thenReturn("Saída registrada com sucesso.");
        String resposta = controller.saidaVeiculo(1L, LocalDateTime.now(), BigDecimal.TEN);
        assertEquals("Saída registrada com sucesso.", resposta);
    }

    @Test
    void deveConsultarVaga() {
        when(garageService.verificaVagaEmUso(1L)).thenReturn("Status da vaga: LIVRE");
        assertEquals("Status da vaga: LIVRE", controller.consultaVaga(1L));
    }

    @Test
    void deveListarTodasVagas() {
        Vaga v1 = new Vaga(), v2 = new Vaga();
        when(garageService.listarTodas()).thenReturn(List.of(v1, v2));
        List<Vaga> vagas = controller.listarTodas();
        assertEquals(2, vagas.size());
    }
}