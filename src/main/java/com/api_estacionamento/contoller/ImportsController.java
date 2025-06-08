package com.api_estacionamento.contoller;

import com.api_estacionamento.dto.ImportsDTO;
import com.api_estacionamento.service.ImportsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/imports")
public class ImportsController {

    @Autowired
    private ImportsService importsService;

    @PostMapping
    public String importarDados(@RequestBody ImportsDTO dto) {
        importsService.importar(dto);
        return "Importação concluída com sucesso.";
    }
}
