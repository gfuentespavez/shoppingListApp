package com.shopping.shoppinglist.service;

import com.shopping.shoppinglist.model.ShoppingItem;
import com.shopping.shoppinglist.model.ShoppingList;
import com.shopping.shoppinglist.repository.ShoppingItemRepository;
import com.shopping.shoppinglist.repository.ShoppingListRepository;
import com.shopping.shoppinglist.util.PDFProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ShoppingListService {
    
    @Autowired
    private ShoppingListRepository shoppingListRepository;
    
    @Autowired
    private ShoppingItemRepository shoppingItemRepository;
    
    @Autowired
    private PDFProcessor pdfProcessor;
    
    public ShoppingList uploadAndProcessPDF(MultipartFile file, String workspaceCode) throws IOException {
        // Guardar archivo temporalmente
        File tempFile = File.createTempFile("shopping-list", ".pdf");
        file.transferTo(tempFile);
        
        // Procesar PDF
        ShoppingList shoppingList = pdfProcessor.processPDF(tempFile);
        
        // Asignar workspace code (o generar uno si no se provee)
        if (workspaceCode == null || workspaceCode.trim().isEmpty()) {
            workspaceCode = "DEFAULT";
        }
        shoppingList.setWorkspaceCode(workspaceCode);
        
        // Guardar en base de datos
        ShoppingList savedList = shoppingListRepository.save(shoppingList);
        
        // Eliminar archivo temporal
        tempFile.delete();
        
        return savedList;
    }
    
    public List<ShoppingList> getAllLists(String workspaceCode) {
        if (workspaceCode == null || workspaceCode.trim().isEmpty()) {
            workspaceCode = "DEFAULT";
        }
        return shoppingListRepository.findByWorkspaceCode(workspaceCode);
    }
    
    public ShoppingList getListById(Long id) {
        return shoppingListRepository.findById(id).orElse(null);
    }
    
    public List<ShoppingItem> getItemsByListId(Long listId) {
        return shoppingItemRepository.findByShoppingListId(listId);
    }
    
    public ShoppingItem updateItemChecked(Long itemId, boolean checked) {
        ShoppingItem item = shoppingItemRepository.findById(itemId).orElse(null);
        if (item != null) {
            item.setChecked(checked);
            return shoppingItemRepository.save(item);
        }
        return null;
    }

    public ShoppingList updateList(ShoppingList list) {
        return shoppingListRepository.save(list);
    }

    public void deleteList(Long id) {
        shoppingListRepository.deleteById(id);
    }

    public Map<String, Integer> getListStats(Long listId) {
        List<ShoppingItem> items = shoppingItemRepository.findByShoppingListId(listId);
        int total = items.size();
        int checked = (int) items.stream().filter(ShoppingItem::isChecked).count();
        int unchecked = total - checked;

        Map<String, Integer> stats = new HashMap<>();
        stats.put("total", total);
        stats.put("checked", checked);
        stats.put("unchecked", unchecked);
        return stats;
    }
}
