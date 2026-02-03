package com.alendre.ambarcrm.model;

public enum StatusCliente {
    NOVO_LEAD("Novo Lead"),
    EM_CONVERSA("Em Conversa"),
    
    // --- NOVOS STATUS (Verifique se estas linhas estão no seu arquivo) ---
    AGENDAMENTO("Agendamento"),
    VISITA_IMOVEL("Visita Imóvel"),
    VISITA_ESCRITORIO("Visita Escritório"),
    PROPOSTA("Proposta"),
    // --------------------------------------------------------------------

    CONDICIONADO("Condicionado"),
    APROVADO("Aprovado"),
    RESTRICAO("Restrição"),
    REPROVADO("Reprovado"), // NOVO
    
    FECHAMENTO("Fechamento"),
    ASSINATURA_CAIXA("Assinatura Caixa"),
    ENTREGA_CHAVES("Entrega de Chaves"),
    VENDA_FINALIZADA("Venda Finalizada"),
    DESISTIU("Desistiu"),
    REATIVACAO("Reativação");

    private final String descricao;

    StatusCliente(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}