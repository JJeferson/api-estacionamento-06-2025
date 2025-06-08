package com.api_estacionamento.services;


import com.api_estacionamento.dto.ImportsDTO;
import com.api_estacionamento.model.RegraAdministrativa;
import com.api_estacionamento.model.Vaga;
import com.api_estacionamento.repository.RegraAdministrativaRepository;
import com.api_estacionamento.repository.VagaRepository;
import com.api_estacionamento.service.ImportsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.List;

import static org.mockito.Mockito.*;

class ImportsServiceTest {

    @InjectMocks
    private ImportsService importsService;

    @Mock
    private VagaRepository vagaRepository;

    @Mock
    private RegraAdministrativaRepository regraRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void deveImportarVagasERegrasQuandoNaoForemVazias() {
        Vaga vaga = new Vaga();
        RegraAdministrativa regra = new RegraAdministrativa();

        ImportsDTO dto = new ImportsDTO();
        dto.setVagas(List.of(vaga));
        dto.setRegras(List.of(regra));

        importsService.importar(dto);

        verify(vagaRepository).saveAll(List.of(vaga));
        verify(regraRepository).saveAll(List.of(regra));
    }

    @Test
    void deveIgnorarImportacaoSeVagasENulasOuVazias() {
        ImportsDTO dto = new ImportsDTO();
        dto.setVagas(null);
        dto.setRegras(List.of());

        importsService.importar(dto);

        verify(vagaRepository, never()).saveAll(any());
        verify(regraRepository, never()).saveAll(any());
    }

    @Test
    void deveImportarApenasVagasQuandoRegrasForemVazias() {
        Vaga vaga = new Vaga();
        ImportsDTO dto = new ImportsDTO();
        dto.setVagas(List.of(vaga));
        dto.setRegras(List.of());

        importsService.importar(dto);

        verify(vagaRepository).saveAll(List.of(vaga));
        verify(regraRepository, never()).saveAll(any());
    }

    @Test
    void deveImportarApenasRegrasQuandoVagasForemVazias() {
        RegraAdministrativa regra = new RegraAdministrativa();
        ImportsDTO dto = new ImportsDTO();
        dto.setVagas(List.of());
        dto.setRegras(List.of(regra));

        importsService.importar(dto);

        verify(regraRepository).saveAll(List.of(regra));
        verify(vagaRepository, never()).saveAll(any());
    }
}
