package com.alendre.ambarcrm.model;

import jakarta.persistence.*;
import org.springframework.format.annotation.DateTimeFormat; // <--- IMPORTANTE: Adicione esta linha lá em cima!
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String cpf;
    private String telefone;
    private String email;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd") // Garante que a data de nascimento não quebre
    private LocalDate dataNascimento;
    
    private LocalDate dataCadastro;

    // --- AQUI ESTÁ A CORREÇÃO! ---
    
    @DateTimeFormat(pattern = "yyyy-MM-dd") // Ensina o Java a ler a DATA do navegador
    private LocalDate dataAgendamento;

    @DateTimeFormat(pattern = "HH:mm")      // Ensina o Java a ler a HORA do navegador
    private LocalTime horaAgendamento;

    private String localAgendamento;
    // -----------------------------

    private String nomeImovel;
    private String fonteCaptacao;
    private String tipoAprovacao; 

    @Enumerated(EnumType.STRING)
    private StatusCliente status;

    @Column(length = 500)
    private String observacao;
    
    
 // --- NOVO MÉTODO: Retorna apenas os números do telefone ---
    public String getTelefoneLimpo() {
        if (telefone == null) return "";
        return telefone.replaceAll("[^0-9]", ""); // Remove tudo que não for número
    }

    // ... (Mantenha os Getters e Setters como estão, não precisa mudar o resto) ...
    
    // GETTERS E SETTERS DOS NOVOS CAMPOS (Caso precise conferir)
    public LocalDate getDataAgendamento() { return dataAgendamento; }
    public void setDataAgendamento(LocalDate dataAgendamento) { this.dataAgendamento = dataAgendamento; }

    public LocalTime getHoraAgendamento() { return horaAgendamento; }
    public void setHoraAgendamento(LocalTime horaAgendamento) { this.horaAgendamento = horaAgendamento; }

    public String getLocalAgendamento() { return localAgendamento; }
    public void setLocalAgendamento(String localAgendamento) { this.localAgendamento = localAgendamento; }

    // (O resto do arquivo continua igual)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public LocalDate getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(LocalDate dataNascimento) { this.dataNascimento = dataNascimento; }
    public LocalDate getDataCadastro() { return dataCadastro; }
    public void setDataCadastro(LocalDate dataCadastro) { this.dataCadastro = dataCadastro; }
    public String getNomeImovel() { return nomeImovel; }
    public void setNomeImovel(String nomeImovel) { this.nomeImovel = nomeImovel; }
    public String getFonteCaptacao() { return fonteCaptacao; }
    public void setFonteCaptacao(String fonteCaptacao) { this.fonteCaptacao = fonteCaptacao; }
    public String getTipoAprovacao() { return tipoAprovacao; }
    public void setTipoAprovacao(String tipoAprovacao) { this.tipoAprovacao = tipoAprovacao; }
    public StatusCliente getStatus() { return status; }
    public void setStatus(StatusCliente status) { this.status = status; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
}