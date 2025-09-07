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
                .filter(row -> !row.isEmpty())
                .map(row -> {
                    Long id = row.size() > 0 ? Long.parseLong(row.get(0).toString()) : null;
                    String name = row.size() > 1 ? row.get(1).toString() : null;
                    int qty = row.size() > 2 ? Integer.parseInt(row.get(2).toString()) : 0;
                    return new Item(id, name, qty);
                })
                .collect(Collectors.toList());
    }
    public void update(Long id, Item item) throws IOException{
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

   public void delete(Long id) throws IOException{
       int rowIndex = findRowById(id);
       if (rowIndex != -1) {
           ClearValuesRequest requestBody = new ClearValuesRequest();
           String range = String.format("%s!A%d:C%d", DATA_SHEET_NAME, rowIndex, rowIndex);
           sheetsService.spreadsheets().values().clear(spreadsheetId, range, requestBody).execute();
           logAction("DELETE", "Item deletado com ID: " + id);
       }
   }
    private int findRowById(Long id) throws IOException {
        String range = DATA_SHEET_NAME + "!A:A";
        ValueRange response = sheetsService.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute();

        List<List<Object>> values = response.getValues();
        if (values != null) {
            // O loop começa em 1 para pular o cabeçalho
            for (int i = 1; i < values.size(); i++) {
                if (!values.get(i).isEmpty() && values.get(i).get(0).toString().equals(id.toString())) {
                    return i + 1; // Retorna o número da linha na planilha (base 1)
                }
            }
        }
        return -1;
    }


    private void logAction(String acao, String detalhes) throws IOException {
        // 1. Pega a data e hora atuais e formata
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        // 2. Cria a lista de valores na ordem correta das colunas
        //    Timestamp -> Coluna A, Ação -> Coluna B, Detalhes -> Coluna C
        ValueRange appendBody = new ValueRange()
                .setValues(List.of(List.of(timestamp, acao, detalhes)));

        // 3. Adiciona a nova linha na aba de Logs
        sheetsService.spreadsheets().values()
                .append(spreadsheetId, LOG_SHEET_NAME, appendBody)
                .setValueInputOption("RAW")
                .execute();
    }

}

