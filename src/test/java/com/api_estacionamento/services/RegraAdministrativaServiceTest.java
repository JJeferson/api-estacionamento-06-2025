package com.api_estacionamento.services;


import com.api_estacionamento.model.RegraAdministrativa;
import com.api_estacionamento.repository.RegraAdministrativaRepository;
import com.api_estacionamento.service.RegraAdministrativaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RegraAdministrativaServiceTest {

    @InjectMocks
    private RegraAdministrativaService service;

    @Mock
    private RegraAdministrativaRepository regraRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void deveListarTodasAsRegras() {
        RegraAdministrativa r1 = new RegraAdministrativa();
        RegraAdministrativa r2 = new RegraAdministrativa();

        when(regraRepository.findAll()).thenReturn(List.of(r1, r2));

        List<RegraAdministrativa> resultado = service.listarTodas();

        assertEquals(2, resultado.size());
        verify(regraRepository).findAll();
    }

    @Test
    void deveBuscarPorIdQuandoExiste() {
        RegraAdministrativa regra = new RegraAdministrativa();
        when(regraRepository.findById(1L)).thenReturn(Optional.of(regra));

        Optional<RegraAdministrativa> resultado = service.buscarPorId(1L);

        assertTrue(resultado.isPresent());
        assertEquals(regra, resultado.get());
        verify(regraRepository).findById(1L);
    }

    @Test
    void deveRetornarVazioQuandoNaoEncontrarId() {
        when(regraRepository.findById(2L)).thenReturn(Optional.empty());

        Optional<RegraAdministrativa> resultado = service.buscarPorId(2L);

        assertFalse(resultado.isPresent());
        verify(regraRepository).findById(2L);
    }

    @Test
    void deveSalvarRegra() {
        RegraAdministrativa regra = new RegraAdministrativa();
        when(regraRepository.save(regra)).thenReturn(regra);

        RegraAdministrativa resultado = service.salvar(regra);

        assertEquals(regra, resultado);
        verify(regraRepository).save(regra);
    }

    @Test
    void deveDeletarPorId() {
        service.deletar(5L);
        verify(regraRepository).deleteById(5L);
    }
}
