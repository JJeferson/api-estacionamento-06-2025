package com.api_estacionamento.services.utils;


import com.api_estacionamento.model.RegraAdministrativa;
import com.api_estacionamento.model.utils.StatusRegra;
import com.api_estacionamento.repository.RegraAdministrativaRepository;
import com.api_estacionamento.service.utils.ValidaHorarioUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.sql.Time;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ValidaHorarioUtilsTest {

    @InjectMocks
    private ValidaHorarioUtils validaHorarioUtils;

    @Mock
    private RegraAdministrativaRepository regraRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void devePermitirEntradaDentroDoHorarioAtivo() {
        RegraAdministrativa regra = new RegraAdministrativa();
        regra.setStatus(StatusRegra.Ativa);
        regra.setHoraEntrada(Time.valueOf(LocalTime.now().minusHours(1)));
        regra.setHoraSaida(Time.valueOf(LocalTime.now().plusHours(1)));

        when(regraRepository.findAll()).thenReturn(List.of(regra));

        boolean permitido = validaHorarioUtils.permitidoEntradaAgora();
        assertTrue(permitido);
    }

    @Test
    void deveRecusarEntradaForaDoHorarioAtivo() {
        RegraAdministrativa regra = new RegraAdministrativa();
        regra.setStatus(StatusRegra.Ativa);
        regra.setHoraEntrada(Time.valueOf(LocalTime.now().minusHours(3)));
        regra.setHoraSaida(Time.valueOf(LocalTime.now().minusHours(1)));

        when(regraRepository.findAll()).thenReturn(List.of(regra));

        boolean permitido = validaHorarioUtils.permitidoEntradaAgora();
        assertFalse(permitido);
    }

    @Test
    void deveRecusarQuandoNaoHaRegrasAtivas() {
        RegraAdministrativa regra = new RegraAdministrativa();
        regra.setStatus(StatusRegra.Desativada);
        regra.setHoraEntrada(Time.valueOf("06:00:00"));
        regra.setHoraSaida(Time.valueOf("22:00:00"));

        when(regraRepository.findAll()).thenReturn(List.of(regra));

        boolean permitido = validaHorarioUtils.permitidoEntradaAgora();
        assertFalse(permitido);
    }

    @Test
    void deveRecusarQuandoHorarioNulo() {
        RegraAdministrativa regra = new RegraAdministrativa();
        regra.setStatus(StatusRegra.Ativa);
        regra.setHoraEntrada(null);
        regra.setHoraSaida(null);

        when(regraRepository.findAll()).thenReturn(List.of(regra));

        boolean permitido = validaHorarioUtils.permitidoEntradaAgora();
        assertFalse(permitido);
    }
}
