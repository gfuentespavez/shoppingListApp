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

        // Intentar formato de listas escolares con categorías
        int itemsFound = 0;
        itemsFound += extractItemsByCategory(text, "MATERIALES ANUALES", list);
        itemsFound += extractItemsByCategory(text, "MATERIALES PERSONALES", list);
        itemsFound += extractItemsByCategory(text, "MATERIALES SEMESTRAL", list);
        itemsFound += extractItemsByCategory(text, "CERTIFICADOS SOLICITADOS", list);

        // Si no se encontraron items con el formato de categorías, intentar formato simple con guiones
        if (itemsFound == 0) {
            extractSimpleListItems(text, list);
        }

        return list;
    }

    private String extractListName(String text) {
        // Intentar formato "NIVEL XXX"
        Pattern nivelPattern = Pattern.compile("NIVEL\\s+([A-Z\\s]+)");
        Matcher nivelMatcher = nivelPattern.matcher(text);
        if (nivelMatcher.find()) {
            return nivelMatcher.group(1).trim();
        }

        // Intentar formato "SUMMER SCHOOL XXXX" o primera línea significativa
        Pattern summerPattern = Pattern.compile("(SUMMER\\s+SCHOOL\\s+\\d+)");
        Matcher summerMatcher = summerPattern.matcher(text);
        if (summerMatcher.find()) {
            return summerMatcher.group(1).trim();
        }

        // Buscar cualquier título en las primeras líneas
        String[] lines = text.split("\\n");
        for (String line : lines) {
            line = line.trim();
            if (line.length() > 5 && line.length() < 100 &&
                    !line.startsWith("-") && !line.startsWith("•")) {
                return line;
            }
        }

        return "Lista sin nombre";
    }

    private int extractItemsByCategory(String text, String categoryName, ShoppingList list) {
        int categoryStart = text.indexOf(categoryName);
        if (categoryStart == -1) return 0;

        // Buscar el siguiente header o fin del texto
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

        int count = 0;
        while (matcher.find()) {
            String quantity = matcher.group(1).trim();
            String description = matcher.group(2).trim().replaceAll("\\s+", " ");

            ShoppingItem item = new ShoppingItem(quantity, description, categoryName);
            list.addItem(item);
            count++;
        }
        return count;
    }

    private void extractSimpleListItems(String text, ShoppingList list) {
        // Patrón para items con guión: -CANTIDAD DESCRIPCIÓN o -DESCRIPCIÓN
        // Ejemplos: -6 PAÑALES DIARIOS, -CEPILLO DE DIENTES NUEVO
        Pattern pattern = Pattern.compile("-\\s*([^\\n]+)");
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            String line = matcher.group(1).trim();

            // Saltar líneas que son solo encabezados o notas
            if (line.isEmpty() ||
                    line.startsWith("RECORDAR") ||
                    line.startsWith("EN CASO DE") ||
                    line.startsWith("OBS:") ||
                    line.contains("RECORDAR ROTULAR") ||
                    line.length() < 5) {
                continue;
            }

            // Intentar extraer cantidad al inicio (número seguido de espacio)
            Pattern qtyPattern = Pattern.compile("^(\\d+)\\s+(.+)");
            Matcher qtyMatcher = qtyPattern.matcher(line);

            String quantity;
            String description;

            if (qtyMatcher.find()) {
                quantity = qtyMatcher.group(1);
                description = qtyMatcher.group(2).trim();
            } else {
                quantity = "1";
                description = line;
            }

            // Limpiar descripciones muy largas (mantener solo la parte principal)
            if (description.length() > 150) {
                int cutPoint = description.indexOf('(');
                if (cutPoint > 20) {
                    description = description.substring(0, cutPoint).trim();
                }
            }

            ShoppingItem item = new ShoppingItem(quantity, description, "General");
            list.addItem(item);
        }
    }
}