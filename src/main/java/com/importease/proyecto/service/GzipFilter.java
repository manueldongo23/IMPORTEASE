package com.importease.proyecto.service;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.zip.GZIPOutputStream;

public class GzipFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String acceptEncoding = req.getHeader("accept-encoding");
        if (acceptEncoding != null && acceptEncoding.contains("gzip")) {
            GzipResponseWrapper gzipResponse = new GzipResponseWrapper(res);
            chain.doFilter(request, gzipResponse);
            gzipResponse.finishResponse();
        } else {
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {
    }

    private static class GzipResponseWrapper extends HttpServletResponseWrapper {
        private ByteArrayOutputStream baos = null;
        private GZIPOutputStream gzipOut = null;
        private ServletOutputStream stream = null;
        private PrintWriter writer = null;

        public GzipResponseWrapper(HttpServletResponse response) {
            super(response);
        }

        @Override
        public ServletOutputStream getOutputStream() throws IOException {
            if (writer != null) {
                throw new IllegalStateException("getWriter() has already been called!");
            }
            if (stream == null) {
                baos = new ByteArrayOutputStream();
                gzipOut = new GZIPOutputStream(baos);
                stream = new ServletOutputStream() {
                    @Override
                    public boolean isReady() { return true; }
                    @Override
                    public void setWriteListener(WriteListener writeListener) {}
                    @Override
                    public void write(int b) throws IOException { gzipOut.write(b); }
                    @Override
                    public void write(byte[] b, int off, int len) throws IOException { gzipOut.write(b, off, len); }
                };
            }
            return stream;
        }

        @Override
        public PrintWriter getWriter() throws IOException {
            if (writer != null) {
                return writer;
            }
            if (stream != null) {
                throw new IllegalStateException("getOutputStream() has already been called!");
            }
            stream = getOutputStream();
            writer = new PrintWriter(new OutputStreamWriter(stream, getResponse().getCharacterEncoding()));
            return writer;
        }

        public void finishResponse() throws IOException {
            if (writer != null) {
                writer.close();
            } else if (stream != null) {
                stream.close();
            }

            if (gzipOut != null) {
                gzipOut.close();
                byte[] compressedBytes = baos.toByteArray();
                HttpServletResponse response = (HttpServletResponse) getResponse();
                response.addHeader("Content-Encoding", "gzip");
                response.setContentLength(compressedBytes.length);
                response.getOutputStream().write(compressedBytes);
            }
        }
    }
}
