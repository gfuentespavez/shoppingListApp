package com.shopping.shoppinglist.util;

import com.shopping.shoppinglist.model.ShoppingItem;
import com.shopping.shoppinglist.model.ShoppingList;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component

public class PDFProcessor {

    public ShoppingList processPDF(File pdfFile) throws IOException {
        PDDocument document = PDDocument.load(pdfFile);
        PDFTextStripper stripper = new PDFTextStripper();
        String text = stripper.getText(document);
        document.close();

        ShoppingList shoppingList = extractShoppingList(text);
        return shoppingList;
    }

    private ShoppingList extractShoppingList(String text) {
        String listName = extractListName(text);
        ShoppingList list = new ShoppingList(listName);

        //Extrear ítems por categoría
        extractItemsByCategory(text, "MATERIALES ANUALES", list);
        extractItemsByCategory(text, "MATERIALES PERSONALES", list);
        extractItemsByCategory(text, "MATERIALES SEMESTRAL", list);
        extractItemsByCategory(text, "CERTIFICADOS SOLICITADOS", list);

        return list;
    }

    private String extractListName(String text) {
        Pattern pattern = Pattern.compile("NIVEL\\s+([A-Z\\s]+)");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return "Lista sin nombre";
    }
    private void extractItemsByCategory(String text, String categoryName, ShoppingList list) {
        int categoryStart = text.indexOf(categoryName);
        if (categoryStart == -1) return;

        //Buscar el siguiente header o fin del texto
        String[] headers = {"MATERIALES ANUALES", "MATERIALES PERSONALES",
                "MATERIALES SEMESTRAL", "CERTIFICADOS SOLICITADOS",
                "TEXTOS EDUCATIVOS"};

        int categoryEnd = text.length();
        for (String header : headers) {
            int headerPos = text.indexOf(header, categoryStart + categoryName.length());
            if (headerPos != -1 && headerPos < categoryEnd) {
                categoryEnd = headerPos;
            }
        }

        String categoryText = text.substring(categoryStart, categoryEnd);

        // Patrón para items con bullet: • 1 descripción
        Pattern pattern = Pattern.compile("•\\s*(\\d+)\\s+([^•]+)");
        Matcher matcher = pattern.matcher(categoryText);

        while (matcher.find()) {
            String quantity = matcher.group(1).trim();
            String description = matcher.group(2).trim().replaceAll("\\s+", " ");

            ShoppingItem item = new ShoppingItem(quantity, description, categoryName);
            list.addItem(item);
        }
    }
}