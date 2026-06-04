package com.importease.proyecto.service;

public class HtmlUtil {
    
    public static String escape(String input) {
        return escapeHtml(input);
    }

    public static String escapeHtml(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#39;")
                    .replace("/", "&#x2F;");
    }

    public static String escapeAttribute(String input) {
        if (input == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (Character.isLetterOrDigit(c)) {
                sb.append(c);
            } else {
                sb.append("&#" + (int) c + ";");
            }
        }
        return sb.toString();
    }

    public static String escapeJavaScript(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\")
                    .replace("'", "\\'")
                    .replace("\"", "\\\"")
                    .replace("\r", "\\r")
                    .replace("\n", "\\n")
                    .replace("/", "\\/")
                    .replace("&", "\\x26");
    }

    public static String escapeUrl(String input) {
        if (input == null) return "";
        try {
            return java.net.URLEncoder.encode(input, "UTF-8");
        } catch (java.io.UnsupportedEncodingException e) {
            return "";
        }
    }
}

