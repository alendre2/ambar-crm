package com.alendre.ambarcrm.service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import com.alendre.ambarcrm.model.Cliente;
import com.alendre.ambarcrm.model.StatusCliente;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;

@Service
public class ExcelService {

    public void exportarClientes(HttpServletResponse response, List<Cliente> todosClientes, 
                                 Integer filtroMes, Integer filtroAno, 
                                 boolean comResumo, boolean comFontes) throws IOException {
        
        List<Cliente> listaParaExcel = new ArrayList<>();
        LocalDate hoje = LocalDate.now();
        
        // 1. DEFINIÇÃO DO PERÍODO
        int targetYear = (filtroAno != null) ? filtroAno : hoje.getYear();
        boolean filtrarPorMesEspecifico = (filtroMes != null && filtroMes > 0);
        int targetMonth = filtrarPorMesEspecifico ? filtroMes : hoje.getMonthValue();
        
        if (filtroMes == null && filtroAno == null) {
            filtrarPorMesEspecifico = true; // Padrão: Mês atual
        }

        // 2. MODO RELATÓRIO
        boolean isRelatorioAtual = (targetYear == hoje.getYear() && targetMonth == hoje.getMonthValue());

        // 3. TÍTULO
        String tituloPeriodo;
        if (filtrarPorMesEspecifico) {
            String nomeMes = Month.of(targetMonth).getDisplayName(TextStyle.FULL, new Locale("pt", "BR")).toUpperCase();
            tituloPeriodo = nomeMes + " " + targetYear;
        } else {
            tituloPeriodo = "ANO DE " + targetYear;
        }

        // =================================================================
        // 4. FILTRAGEM (QUEM ENTRA NA LISTA?)
        // =================================================================
        for (Cliente c : todosClientes) {
            StatusCliente st = c.getStatus();
            
            boolean isFinalizado = (
                st == StatusCliente.VENDA_FINALIZADA || 
                st == StatusCliente.DESISTIU ||
                st == StatusCliente.ENTREGA_CHAVES ||
                st == StatusCliente.ASSINATURA_CAIXA || 
                st == StatusCliente.FECHAMENTO
            );
            
            boolean isDoPeriodo = false;
            if (c.getDataCadastro() != null) {
                boolean mesmoAno = c.getDataCadastro().getYear() == targetYear;
                boolean mesmoMes = (!filtrarPorMesEspecifico) || c.getDataCadastro().getMonthValue() == targetMonth;
                isDoPeriodo = mesmoAno && mesmoMes;
            }

            if (isRelatorioAtual) {
                if (!isFinalizado || isDoPeriodo) listaParaExcel.add(c);
            } else {
                if (isDoPeriodo) listaParaExcel.add(c);
            }
        }
        
        // ===========================================
        // GERAÇÃO DA PLANILHA
        // ===========================================
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Relatório");

        // Estilos
        CellStyle headerBlue = createStyle(workbook, IndexedColors.ROYAL_BLUE, true);
        CellStyle headerLightBlue = createStyle(workbook, IndexedColors.LIGHT_BLUE, true);
        CellStyle headerGray = createStyle(workbook, IndexedColors.GREY_25_PERCENT, true);
        CellStyle dataStyle = createBorderedStyle(workbook);
        CellStyle stAmarelo = createColorStyle(workbook, IndexedColors.LEMON_CHIFFON);
        CellStyle stVerde = createColorStyle(workbook, IndexedColors.LIGHT_GREEN);
        CellStyle stVermelho = createColorStyle(workbook, IndexedColors.ROSE);
        CellStyle stCinza = createColorStyle(workbook, IndexedColors.GREY_25_PERCENT);

        // Título
        String dataGeracao = hoje.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String textoTitulo = "CONTROLE DE PROPOSTA " + tituloPeriodo + " - CORRETOR CIBELLY GOMES (Gerado em " + dataGeracao + ")";
        Row titleRow = sheet.createRow(0);
        titleRow.setHeightInPoints(25);
        createCell(titleRow, 0, textoTitulo, headerBlue);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 7));

        // Cabeçalho
        Row headerRow = sheet.createRow(1);
        String[] colunas = {"DATA/PROPOSTA", "CLIENTE", "CPF DO CLIENTE", "IMÓVEL", "APROVAÇÃO", "SITUAÇÃO", "FONTE", "TELEFONE"};
        for (int i = 0; i < colunas.length; i++) createCell(headerRow, i, colunas[i], headerGray);

        int rowIdx = 2;
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        
        // VARIÁVEIS DE CONTAGEM
        int qtdContatos=0, qtdAgendamento=0, qtdVisitaImovel=0, qtdVisitaEscritorio=0, qtdPropostas=0, qtdAprovadas=0, qtdVenda=0;
        Map<String, Integer> fontesCount = new HashMap<>();

        // LOOP DE PREENCHIMENTO E CONTAGEM
        for (Cliente c : listaParaExcel) {
            Row row = sheet.createRow(rowIdx++);
            createCell(row, 0, c.getDataCadastro() != null ? c.getDataCadastro().format(fmt) : "", dataStyle);
            createCell(row, 1, c.getNome(), dataStyle);
            createCell(row, 2, c.getCpf(), dataStyle);
            createCell(row, 3, c.getNomeImovel() != null ? c.getNomeImovel() : "A DEFINIR", dataStyle);
            createCell(row, 4, c.getTipoAprovacao() != null ? c.getTipoAprovacao() : "", dataStyle);
            
            Cell cellSt = row.createCell(5);
            cellSt.setCellValue(c.getStatus().getDescricao());
            applyColorToStatus(c.getStatus(), cellSt, stVerde, stAmarelo, stVermelho, stCinza);

            String fonte = c.getFonteCaptacao() != null ? c.getFonteCaptacao() : "OUTROS";
            createCell(row, 6, fonte, dataStyle);
            createCell(row, 7, c.getTelefone(), dataStyle);

            // =====================================================
            // CONTAGEM ESTRITA ✅
            // =====================================================
            
            qtdContatos++; // Conta TODOS da lista (inclusive "Em Conversa")

            StatusCliente st = c.getStatus();
            
            // 1. AGENDAMENTO (Agora SÓ conta se for status AGENDAMENTO)
            if (st == StatusCliente.AGENDAMENTO) {
                qtdAgendamento++;
            }
            
            // 2. VISITAS
            if (st == StatusCliente.VISITA_IMOVEL) qtdVisitaImovel++;
            if (st == StatusCliente.VISITA_ESCRITORIO) qtdVisitaEscritorio++;

            // 3. PROPOSTAS (Total rodando ou respondidas)
            if (st == StatusCliente.PROPOSTA || 
                st == StatusCliente.APROVADO || 
                st == StatusCliente.CONDICIONADO || 
                st == StatusCliente.RESTRICAO || 
                st == StatusCliente.REPROVADO) {
                qtdPropostas++;
            }
            
            // 4. APROVADAS
            if (st == StatusCliente.APROVADO) qtdAprovadas++;
            
            // 5. VENDA
            if (st == StatusCliente.ASSINATURA_CAIXA || st == StatusCliente.FECHAMENTO) {
                qtdVenda++;
            }

            fontesCount.put(fonte, fontesCount.getOrDefault(fonte, 0) + 1);
        }
        for (int i = 0; i < colunas.length; i++) sheet.autoSizeColumn(i);

        // RODAPÉ
        rowIdx += 2;
        int startRow = rowIdx;

        if (comResumo) {
            createSummaryTable(sheet, startRow, "NÚMEROS", headerLightBlue, dataStyle, new Object[][]{
                {"CONTATOS", qtdContatos}, 
                {"AGENDAMENTO", qtdAgendamento}, 
                {"VISITAS ESCRITÓRIO", qtdVisitaEscritorio}, 
                {"VISITAS IMÓVEL", qtdVisitaImovel},
                {"PROPOSTAS", qtdPropostas}, 
                {"PROPOSTA APROV.", qtdAprovadas}, 
                {"VENDA", qtdVenda}
            });

            int legendRow = startRow;
            createMergedHeader(sheet, legendRow, 3, 4, "ORGANIZAÇÃO DA TABELA", headerLightBlue); legendRow++;
            createLegendRow(sheet, legendRow++, 3, stAmarelo, "EM ANÁLISE");
            createLegendRow(sheet, legendRow++, 3, stVerde, "APROVADOS");
            createLegendRow(sheet, legendRow++, 3, stCinza, "CONDICIONADO");
            createLegendRow(sheet, legendRow++, 3, stVermelho, "REPROVADO");
            
            startRow = Math.max(startRow + 8, legendRow + 1); 
        }

        if (comFontes) {
            int fonteRow = comResumo ? startRow : rowIdx;
            createMergedHeader(sheet, fonteRow, 3, 4, "FONTE", headerBlue);
            fonteRow++;
            for (Map.Entry<String, Integer> entry : fontesCount.entrySet()) {
                Row r = getOrCreateRow(sheet, fonteRow++);
                createCell(r, 3, entry.getKey(), dataStyle);
                createCell(r, 4, entry.getValue().toString(), dataStyle);
            }
        }

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();
        outputStream.close();
    }

    // --- MÉTODOS AUXILIARES ---
    private void createSummaryTable(Sheet sheet, int startRow, String title, CellStyle headStyle, CellStyle dataStyle, Object[][] data) {
        createMergedHeader(sheet, startRow, 0, 1, title, headStyle);
        int r = startRow + 1;
        for (Object[] rowData : data) {
            Row row = getOrCreateRow(sheet, r++);
            createCell(row, 0, rowData[0].toString(), dataStyle);
            createCell(row, 1, rowData[1].toString(), dataStyle);
        }
    }
    private void createLegendRow(Sheet sheet, int rowNum, int col, CellStyle colorStyle, String text) {
        Row row = getOrCreateRow(sheet, rowNum);
        Cell cellColor = row.createCell(col); cellColor.setCellStyle(colorStyle);
        Cell cellText = row.createCell(col + 1); cellText.setCellValue(text);
        CellStyle border = sheet.getWorkbook().createCellStyle();
        border.setBorderBottom(BorderStyle.THIN); border.setBorderTop(BorderStyle.THIN);
        border.setBorderRight(BorderStyle.THIN); border.setBorderLeft(BorderStyle.THIN);
        cellText.setCellStyle(border);
    }
    private void createMergedHeader(Sheet sheet, int rowNum, int colStart, int colEnd, String text, CellStyle style) {
        Row row = getOrCreateRow(sheet, rowNum);
        Cell cell = row.createCell(colStart); cell.setCellValue(text); cell.setCellStyle(style);
        sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, colStart, colEnd));
    }
    private Row getOrCreateRow(Sheet sheet, int rowIdx) { Row row = sheet.getRow(rowIdx); return (row == null) ? sheet.createRow(rowIdx) : row; }
    private void createCell(Row row, int col, String val, CellStyle style) { Cell c = row.createCell(col); c.setCellValue(val); c.setCellStyle(style); }
    private CellStyle createColorStyle(Workbook wb, IndexedColors color) {
        CellStyle style = wb.createCellStyle(); style.setFillForegroundColor(color.getIndex()); style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN); style.setBorderTop(BorderStyle.THIN); style.setBorderRight(BorderStyle.THIN); style.setBorderLeft(BorderStyle.THIN); return style;
    }
    private CellStyle createStyle(Workbook wb, IndexedColors color, boolean bold) {
        CellStyle style = wb.createCellStyle(); style.setFillForegroundColor(color.getIndex()); style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER); Font font = wb.createFont(); font.setColor(IndexedColors.WHITE.getIndex()); if (bold) font.setBold(true); style.setFont(font); return style;
    }
    private CellStyle createBorderedStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle(); style.setBorderBottom(BorderStyle.THIN); style.setBorderTop(BorderStyle.THIN); style.setBorderRight(BorderStyle.THIN); style.setBorderLeft(BorderStyle.THIN); style.setAlignment(HorizontalAlignment.CENTER); return style;
    }
    private void applyColorToStatus(StatusCliente s, Cell cell, CellStyle verde, CellStyle amarelo, CellStyle vermelho, CellStyle cinza) {
        String name = s.name();
        if(name.equals("APROVADO") || name.equals("VENDA_FINALIZADA") || name.equals("ENTREGA_CHAVES") || name.equals("FECHAMENTO") || name.equals("ASSINATURA_CAIXA")) cell.setCellStyle(verde);
        else if (name.equals("DESISTIU") || name.equals("RESTRICAO") || name.equals("REPROVADO")) cell.setCellStyle(vermelho);
        else if (name.equals("CONDICIONADO")) cell.setCellStyle(cinza);
        else cell.setCellStyle(amarelo);
    }
}