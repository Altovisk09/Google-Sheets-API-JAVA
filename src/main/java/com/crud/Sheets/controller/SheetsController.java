package com.crud.Sheets.controller;

import com.crud.Sheets.model.Item;
import com.crud.Sheets.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/items")
public class SheetsController {

    @Autowired
    private ItemService itemService;

    @PostMapping
    public ResponseEntity<String> create(@RequestBody Item item) throws IOException {
        itemService.create(item);
        return ResponseEntity.ok("Item criado com sucesso.");
    }

    @GetMapping
    public ResponseEntity<List<Item>> getAll() throws IOException {
        return ResponseEntity.ok(itemService.readAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> update(@PathVariable Long id, @RequestBody Item item) throws IOException {
        itemService.update(id, item);
        return ResponseEntity.ok("Item atualizado com sucesso.");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) throws IOException {
        itemService.delete(id);
        return ResponseEntity.ok("Item deletado com sucesso.");
    }
}