package com.alendre.ambarcrm.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.alendre.ambarcrm.model.Cliente;
import com.alendre.ambarcrm.model.StatusCliente;
import com.alendre.ambarcrm.repository.ClienteRepository;
import com.alendre.ambarcrm.service.ExcelService; 

import jakarta.servlet.http.HttpServletResponse; 

@Controller
public class ClienteController {

    @Autowired
    private ClienteRepository repository;

    @Autowired
    private ExcelService excelService;

    @GetMapping("/")
    public ModelAndView listar(@RequestParam(required = false) String busca,
                               @RequestParam(required = false) StatusCliente status) {
        ModelAndView mv = new ModelAndView("home");
        
        List<Cliente> clientes;
        
        if (busca != null && !busca.isEmpty()) {
            clientes = repository.findByNomeContainingIgnoreCaseOrderByIdDesc(busca);
        } else if (status != null) {
            clientes = repository.findByStatusOrderByIdDesc(status);
        } else {
            clientes = repository.findAllByOrderByIdDesc();
        }
        
        mv.addObject("listaClientes", clientes);
        mv.addObject("termoBusca", busca);
        mv.addObject("totalClientes", clientes.size()); 
        
        int mesAtual = LocalDate.now().getMonthValue();
        List<Cliente> aniversariantes = new ArrayList<>();
        
        for (Cliente c : clientes) {
            if (c.getDataNascimento() != null && c.getDataNascimento().getMonthValue() == mesAtual) {
                aniversariantes.add(c);
            }
        }
        mv.addObject("aniversariantes", aniversariantes);
        
        Map<String, Long> contagemPorStatus = clientes.stream()
            .collect(Collectors.groupingBy(c -> c.getStatus().getDescricao(), Collectors.counting()));
        
        mv.addObject("graficoLabels", new ArrayList<>(contagemPorStatus.keySet()));
        mv.addObject("graficoDados", new ArrayList<>(contagemPorStatus.values()));
        
        return mv;
    }
    
    @GetMapping("/funil")
    public ModelAndView funil() {
        ModelAndView mv = new ModelAndView("pipeline");
        List<Cliente> clientes = repository.findAllByOrderByIdDesc();
        mv.addObject("listaClientes", clientes);
        return mv;
    }

    // --- CORREÇÃO AQUI: Mudou de "formulario" para "cadastro" ---
    @GetMapping("/novo")
    public ModelAndView novo() {
        ModelAndView mv = new ModelAndView("cadastro"); // <--- AQUI O SEGREDO
        mv.addObject("cliente", new Cliente());
        mv.addObject("listaStatus", StatusCliente.values());
        return mv;
    }

    @PostMapping("/salvar")
    public String salvar(Cliente cliente) {
        if (cliente.getId() != null) {
            Cliente clienteExistente = repository.findById(cliente.getId()).orElse(null);
            
            if (clienteExistente != null) {
                cliente.setDataCadastro(clienteExistente.getDataCadastro());
            }
        } else {
            if (cliente.getDataCadastro() == null) {
                cliente.setDataCadastro(LocalDate.now());
            }
        }

        repository.save(cliente);
        return "redirect:/";
    }
    
 // ROTA PARA VISUALIZAR DETALHES (MODO LEITURA)
    @GetMapping("/detalhes/{id}")
    public ModelAndView detalhes(@PathVariable Long id) {
        ModelAndView mv = new ModelAndView("detalhes"); // Vai abrir o arquivo detalhes.html
        java.util.Optional<Cliente> cliente = repository.findById(id);
        
        if (cliente.isPresent()) {
            mv.addObject("cliente", cliente.get());
        } else {
            return new ModelAndView("redirect:/");
        }
        return mv;
    }
    
    // --- CORREÇÃO AQUI: Mudou de "formulario" para "cadastro" ---
    @GetMapping("/editar/{id}")
    public ModelAndView editar(@PathVariable Long id) {
        ModelAndView mv = new ModelAndView("cadastro"); // <--- AQUI O SEGREDO
        java.util.Optional<Cliente> cliente = repository.findById(id);
        
        if (cliente.isPresent()) {
            mv.addObject("cliente", cliente.get());
        } else {
            mv.addObject("cliente", new Cliente());
        }
        mv.addObject("listaStatus", StatusCliente.values());
        return mv;
    }
    
    @GetMapping("/excluir/{id}")
    public String excluir(@PathVariable Long id) {
        repository.deleteById(id);
        return "redirect:/";
    }

    @GetMapping("/exportar")
    public void exportarParaExcel(HttpServletResponse response,
                                  @RequestParam(value = "mes", required = false) Integer mes,
                                  @RequestParam(value = "ano", required = false) Integer ano,
                                  @RequestParam(value = "comResumo", defaultValue = "true") boolean comResumo,
                                  @RequestParam(value = "comFontes", defaultValue = "true") boolean comFontes) throws IOException {
        
        response.setContentType("application/octet-stream");
        String headerKey = "Content-Disposition";
        
        String filename = "relatorio_geral.xlsx";
        if (ano != null) {
            if (mes != null) filename = "relatorio_" + mes + "_" + ano + ".xlsx";
            else filename = "relatorio_" + ano + ".xlsx";
        }
        
        String headerValue = "attachment; filename=" + filename;
        response.setHeader(headerKey, headerValue);

        List<Cliente> listaClientes = repository.findAllByOrderByIdDesc();
        excelService.exportarClientes(response, listaClientes, mes, ano, comResumo, comFontes);
    }
    
 // --- NOVO: Rota para a tela de Login Personalizada ---
    @GetMapping("/login")
    public String login() {
        return "login"; // Isso chama o arquivo login.html
    }
}