package com.api_estacionamento.contoller;

import com.api_estacionamento.model.Vaga;
import com.api_estacionamento.service.GarageService;
import com.api_estacionamento.service.utils.ValidaHorarioUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/garage")
public class GarageController {

    @Autowired
    private GarageService garageService;

    @Autowired
    private ValidaHorarioUtils validaHorarioUtils;

    private boolean garagemAberta = false;

    @PutMapping
    public String alternarGaragem() {
        garagemAberta = !garagemAberta;
        if (!garagemAberta) {
            garageService.liberarTodasAsVagas();
            return "Garagem fechada e todas as vagas foram liberadas.";
        }
        return "Garagem aberta.";
    }

    @PostMapping
    public ResponseEntity<String> entradaVeiculo(@RequestParam Long idVaga,
                                                 @RequestParam LocalDateTime entrada,
                                                 @RequestParam BigDecimal tarifa) {
        if (!garagemAberta) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Garagem está fechada.");
        }

        if (!validaHorarioUtils.permitidoEntradaAgora()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("A entrada não é permitida fora do horário autorizado pelas regras administrativas.");
        }

        try {
            String resposta = garageService.entradaVeiculo(idVaga, entrada, tarifa);
            return ResponseEntity.ok(resposta);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
        }
    }

    @PatchMapping
    public String saidaVeiculo(@RequestParam Long idVaga,
                               @RequestParam LocalDateTime saida,
                               @RequestParam BigDecimal valorPago) {
        if (!garagemAberta) return "Garagem está fechada.";
        return garageService.saidaVeiculo(idVaga, saida, valorPago);
    }

    @GetMapping("/{idVaga}")
    public String consultaVaga(@PathVariable Long idVaga) {
        return garageService.verificaVagaEmUso(idVaga);
    }

    @GetMapping
    public List<Vaga> listarTodas() {
        return garageService.listarTodas();
    }
}