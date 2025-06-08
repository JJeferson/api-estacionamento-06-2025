package com.api_estacionamento.controllers;


import com.api_estacionamento.contoller.ImportsController;
import com.api_estacionamento.dto.ImportsDTO;
import com.api_estacionamento.service.ImportsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ImportsControllerTest {

    @InjectMocks
    private ImportsController controller;

    @Mock
    private ImportsService importsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void deveImportarDadosComSucesso() {
        ImportsDTO dto = new ImportsDTO(); // vazio, mas suficiente pro teste do controller

        String resposta = controller.importarDados(dto);

        verify(importsService).importar(dto);
        assertEquals("Importação concluída com sucesso.", resposta);
    }
}
