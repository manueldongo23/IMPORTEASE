<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="true" %>
<%@ page import="java.util.*" %>
<% 
    List<Map<String, String>> crumbs = (List<Map<String, String>>) request.getAttribute("breadcrumb");
    if (crumbs == null) {
        crumbs = new ArrayList<>();
    }
%>
<nav aria-label="Miga de pan" class="breadcrumb flex flex-wrap items-center gap-2 text-sm text-[var(--text-tertiary)] px-8 py-3 bg-[var(--surface-1)]/80 backdrop-blur-xl border-b border-[var(--border)] z-20">
  <% 
    for (int i = 0; i < crumbs.size(); i++) {
        Map<String, String> crumb = crumbs.get(i);
        String label = com.importease.proyecto.service.HtmlUtil.escape(crumb.get("label"));
        String url = crumb.get("url");
        boolean isLast = (i == crumbs.size() - 1);
        
        if (!isLast && url != null && !url.trim().isEmpty()) {
  %>
        <a href="<%= url %>" class="hover:underline transition-colors duration-200" title="Ir a <%= label %>">
          <%= label %>
        </a>
        <svg class="w-3 h-3 text-[var(--border-strong)]" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" viewBox="0 0 24 24" aria-hidden="true">
          <path d="M9 5l7 7-7 7" />
        </svg>
  <% 
        } else {
  %>
        <span aria-current="page" class="font-medium text-[var(--text-primary)]">
          <%= label %>
        </span>
  <% 
        }
    }
  %>
</nav>
<!-- Optional progress bar -->
<div class="breadcrumb-progress w-full"></div>
