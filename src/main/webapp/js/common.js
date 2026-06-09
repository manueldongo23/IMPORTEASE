// common.js – shared utilities, toast handling, AJAX wrapper

/**
 * Simple wrapper for fetch with CSRF token and JSON handling.
 */
async function apiFetch(url, options = {}) {
  const ctx = window.ImportEase && window.ImportEase.ctx ? window.ImportEase.ctx : (window.ctx || '');
  const headers = {
    'Content-Type': 'application/json',
    'X-CSRF-TOKEN': window.csrfToken || '',
    ...options.headers,
  };
  const response = await fetch(ctx + url, { ...options, headers });
  if (!response.ok) {
    const err = await response.text();
    console.error('API error', response.status, err);
    throw new Error(err || 'Server error');
  }
  return response.json();
}

/**
 * Show a simple toast message (info, success, warning, error).
 */
function showToast(message, type = 'info') {
  const toastContainer = document.getElementById('toastContainer');
  if (!toastContainer) return;
  const toast = document.createElement('div');
  toast.className = `alert alert-${type} shadow`;
  toast.textContent = message;
  toastContainer.appendChild(toast);
  setTimeout(() => toast.remove(), 5000);
}
