package com.importease.proyecto.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public class XssRequestWrapper extends HttpServletRequestWrapper {

    public XssRequestWrapper(HttpServletRequest servletRequest) {
        super(servletRequest);
    }

    @Override
    public String[] getParameterValues(String parameter) {
        String[] values = super.getParameterValues(parameter);

        if (values == null) {
            return null;
        }

        int count = values.length;
        String[] encodedValues = new String[count];
        for (int i = 0; i < count; i++) {
            encodedValues[i] = stripXSS(values[i]);
        }

        return encodedValues;
    }

    @Override
    public String getParameter(String parameter) {
        String value = super.getParameter(parameter);
        return stripXSS(value);
    }

    @Override
    public String getHeader(String name) {
        String value = super.getHeader(name);
        return stripXSS(value);
    }

    private String stripXSS(String value) {
        if (value != null) {
            // NOTE: It's highly recommended to use the ESAPI library and uncomment the following line to
            // avoid encoded attacks.
            // value = ESAPI.encoder().canonicalize(value);

            // Avoid null characters
            value = value.replaceAll("\0", "");

            // Avoid anything between script tags
            value = value.replaceAll("(?i)<script.*?>.*?</script.*?>", "");

            // Avoid anything in a src='...' type of expression
            value = value.replaceAll("(?i)src[\r\n]*=[\r\n]*\\\'(.*?)\\\'", "");
            value = value.replaceAll("(?i)src[\r\n]*=[\r\n]*\\\"(.*?)\\\"", "");

            // Remove any lonesome </script> tag
            value = value.replaceAll("(?i)</script>", "");

            // Remove any lonesome <script ...> tag
            value = value.replaceAll("(?i)<script.*?>", "");

            // Avoid eval(...) expressions
            value = value.replaceAll("(?i)eval\\((.*?)\\)", "");

            // Avoid expression(...) expressions
            value = value.replaceAll("(?i)expression\\((.*?)\\)", "");

            // Avoid javascript:... expressions
            value = value.replaceAll("(?i)javascript:", "");

            // Avoid vbscript:... expressions
            value = value.replaceAll("(?i)vbscript:", "");

            // Avoid onload= expressions
            value = value.replaceAll("(?i)onload(.*?)=", "");

            // Avoid onfocus= expressions
            value = value.replaceAll("(?i)onfocus(.*?)=", "");
        }
        return value;
    }
}
