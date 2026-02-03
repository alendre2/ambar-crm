package com.alendre.ambarcrm.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class SimuladorController {

    @GetMapping("/simulador")
    public String abrirSimulador() {
        return "simulador";
    }

    @PostMapping("/simular")
    public ModelAndView calcular(@RequestParam("renda") Double renda) {
        
        ModelAndView mv = new ModelAndView("simulador");
        
        // CÁLCULO EXATO: 30% da Renda (Lei do Comprometimento de Renda)
        double parcelaMaxima = renda * 0.30;
        
        mv.addObject("renda", renda);
        mv.addObject("resultado", true);
        mv.addObject("parcelaMaxima", parcelaMaxima);
        
        return mv;
    }
}