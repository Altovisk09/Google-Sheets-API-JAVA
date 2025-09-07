package com.crud.Sheets.service;

import com.crud.Sheets.model.Item;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ClearValuesRequest;
import com.google.api.services.sheets.v4.model.ValueRange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ItemService {
    @Autowired
    private Sheets sheetsService;
    @Value("${google.spreadsheet.id}")
    private String spreadsheetId;

    private static final String DATA_SHEET_NAME = "DB";
    private static final String LOG_SHEET_NAME = "Logs";

    public void create(Item item) throws IOException {
        ValueRange appendBody = new ValueRange()
                .setValues(List.of(List.of(item.getId(), item.getName(), item.getQuantity())));
        sheetsService.spreadsheets().values()
                .append(spreadsheetId, DATA_SHEET_NAME, appendBody)
                .setValueInputOption("RAW")
                .execute();
        logAction("CREATE", "Item criado: " + item.toString());
    }

    public List<Item> readAll() throws IOException {
        ValueRange response = sheetsService.spreadsheets().values()
                .get(spreadsheetId, DATA_SHEET_NAME)
                .execute();

        List<List<Object>> values = response.getValues();
        if (values == null || values.isEmpty()) {
            return List.of();
        }

        return values.stream()
                .skip(1) // pula cabeçalho
                .filter(row -> !row.isEmpty() && row.size() > 0) // Garante que a linha e a primeira coluna não são vazias
                .map(row -> {
                    // Trata o caso de a célula do ID estar vazia ou não ser um número
                    Long id = null;
                    try {
                        id = Long.parseLong(row.get(0).toString());
                    } catch (NumberFormatException e) {
                        // Ignora a linha se o ID não for um número válido
                        return null;
                    }
                    String name = row.size() > 1 ? row.get(1).toString() : "Nome não disponível";
                    int qty = row.size() > 2 ? Integer.parseInt(row.get(2).toString()) : 0;
                    return new Item(id, name, qty);
                })
                .filter(item -> item != null) // Remove as linhas que foram invalidadas (ID não numérico)
                .collect(Collectors.toList());
    }

    public void update(Long id, Item item) throws IOException {
        int rowIndex = findRowById(id);
        if (rowIndex == -1) {
            throw new RuntimeException("Item não encontrado com o ID: " + id);
        }
        String range = String.format("%s!A%d:C%d", DATA_SHEET_NAME, rowIndex, rowIndex);
        ValueRange updateBody = new ValueRange()
                .setValues(List.of(List.of(item.getId(), item.getName(), item.getQuantity())));

        sheetsService.spreadsheets().values()
                .update(spreadsheetId, range, updateBody)
                .setValueInputOption("RAW")
                .execute();
        logAction("UPDATE", "Item atualizado: " + item.toString());
    }

    public void delete(Long id) throws IOException {
        int rowIndex = findRowById(id);
        if (rowIndex != -1) {
            ClearValuesRequest requestBody = new ClearValuesRequest();
            String range = String.format("%s!A%d:C%d", DATA_SHEET_NAME, rowIndex, rowIndex);
            sheetsService.spreadsheets().values().clear(spreadsheetId, range, requestBody).execute();
            logAction("DELETE", "Item deletado com ID: " + id);
        }
    }

    public void createBatch(List<Item> items) throws IOException {
        List<List<Object>> rows = items.stream()
                .map(item -> {
                    List<Object> row = new ArrayList<>();
                    row.add(item.getId());
                    row.add(item.getName());
                    row.add(item.getQuantity());
                    return row;
                })
                .collect(Collectors.toList());

        ValueRange appendBody = new ValueRange().setValues(rows);
        sheetsService.spreadsheets().values()
                .append(spreadsheetId, DATA_SHEET_NAME, appendBody)
                .setValueInputOption("RAW")
                .execute();

        logAction("CREATE_BATCH", items.size() + " itens criados.");
    }
    private int findRowById(Long id) throws IOException {
        String range = DATA_SHEET_NAME + "!A:A";
        ValueRange response = sheetsService.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute();

        List<List<Object>> values = response.getValues();
        if (values != null) {
            for (int i = 1; i < values.size(); i++) { // Começa em 1 para pular o cabeçalho
                if (values.get(i) != null && !values.get(i).isEmpty()) {
                    try {
                        // MELHORIA: Compara os valores como números para maior precisão
                        Long cellId = Long.parseLong(values.get(i).get(0).toString());
                        if (cellId.equals(id)) {
                            return i + 1;
                        }
                    } catch (NumberFormatException e) {
                        // Ignora a linha se o ID na planilha não for um número.
                        // Isso evita que a aplicação quebre se alguém digitar texto na coluna de ID.
                        continue;
                    }
                }
            }
        }
        return -1;
    }

    private void logAction(String acao, String detalhes) throws IOException {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        ValueRange appendBody = new ValueRange()
                .setValues(List.of(List.of(timestamp, acao, detalhes)));

        sheetsService.spreadsheets().values()
                .append(spreadsheetId, LOG_SHEET_NAME, appendBody)
                .setValueInputOption("RAW")
                .execute();
    }
}