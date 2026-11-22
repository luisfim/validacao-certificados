package br.com.luis.certificados;

import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.Locale;

public class Texto {
    public static String normalizar(String s) {
        if (s == null) return "";
        return Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT).trim().replaceAll("\\s+", " ");
    }
    public static boolean vazio(String s) { return s == null || s.isBlank(); }
    public static LocalDate data(String s) {
        if (vazio(s)) return null;
        for (var f : new DateTimeFormatter[]{DateTimeFormatter.ISO_LOCAL_DATE,
                DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT)}) {
            try { return LocalDate.parse(s.trim(), f); } catch (Exception ignored) { }
        }
        return null;
    }
}
