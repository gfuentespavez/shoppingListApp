package com.shopping.shoppinglist.controller;

import com.shopping.shoppinglist.model.ShoppingItem;
import com.shopping.shoppinglist.model.ShoppingList;
import com.shopping.shoppinglist.service.ShoppingListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ShoppingListController {
    
    @Autowired
    private ShoppingListService shoppingListService;
    
    @PostMapping("/upload")
    public ResponseEntity<ShoppingList> uploadPDF(@RequestParam("file") MultipartFile file) {
        try {
            ShoppingList shoppingList = shoppingListService.uploadAndProcessPDF(file);
            return ResponseEntity.ok(shoppingList);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/lists")
    public ResponseEntity<List<ShoppingList>> getAllLists() {
        List<ShoppingList> lists = shoppingListService.getAllLists();
        return ResponseEntity.ok(lists);
    }
    
    @GetMapping("/lists/{id}")
    public ResponseEntity<ShoppingList> getListById(@PathVariable Long id) {
        ShoppingList list = shoppingListService.getListById(id);
        return ResponseEntity.ok(list);
    }
    
    @GetMapping("/lists/{id}/items")
    public ResponseEntity<List<ShoppingItem>> getItemsByListId(@PathVariable Long id) {
        List<ShoppingItem> items = shoppingListService.getItemsByListId(id);
        return ResponseEntity.ok(items);
    }
    
    @PatchMapping("/items/{id}")
    public ResponseEntity<ShoppingItem> updateItemChecked(
            @PathVariable Long id, 
            @RequestBody UpdateItemRequest request) {
        ShoppingItem item = shoppingListService.updateItemChecked(id, request.isChecked());
        return ResponseEntity.ok(item);
    }
    
    static class UpdateItemRequest {
        private boolean checked;
        
        public boolean isChecked() {
            return checked;
        }
        
        public void setChecked(boolean checked) {
            this.checked = checked;
        }
    }

    @PatchMapping("/lists/{id}")
    public ResponseEntity<ShoppingList> updateListName(
            @PathVariable Long id,
            @RequestBody UpdateListNameRequest request) {
        ShoppingList list =shoppingListService.getListById(id);
        if ( list == null) {
            return ResponseEntity.notFound().build();
        }

        list.setName(request.getName());
        ShoppingList updated = shoppingListService.updateList(list);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/lists/{id}/stats")
    public ResponseEntity<Map<String, Integer>> getListStats(@PathVariable Long id) {
        Map<String, Integer> stats = shoppingListService.getListStats(id);
        return ResponseEntity.ok(stats);
    }

    static class UpdateListNameRequest {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
