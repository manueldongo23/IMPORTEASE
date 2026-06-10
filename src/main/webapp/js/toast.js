function showNotification(title, message, isSuccess) {
    if (isSuccess === void 0) { isSuccess = false; }
    var toast = document.getElementById('toastNotification');
    if (!toast) return;
    toast.setAttribute('role', 'alert');
    toast.setAttribute('aria-live', 'polite');
    var icon = document.getElementById('toastIcon');
    var tTitle = document.getElementById('toastTitle');
    var tMsg = document.getElementById('toastMessage');
    
    if (icon) icon.innerText = isSuccess ? '✨' : '⚠️';
    if (tTitle) {
        tTitle.innerText = title;
        tTitle.style.color = isSuccess ? '#10b981' : '#ef4444';
    }
    if (tMsg) tMsg.innerText = message;
    
    // Add visibility classes for both styling systems
    toast.classList.add('active');
    toast.classList.add('show');
    toast.classList.add('toast-active');
    
    // Explicit inline styles to guarantee display regardless of stylesheet loaded
    toast.style.opacity = '1';
    toast.style.transform = 'translate(-50%, 0) scale(1)';
    toast.style.pointerEvents = 'auto';
    
    setTimeout(function () {
        toast.classList.remove('active');
        toast.classList.remove('show');
        toast.classList.remove('toast-active');
        toast.style.opacity = '0';
        toast.style.transform = 'translate(-50%, -20px) scale(0.95)';
        toast.style.pointerEvents = 'none';
    }, 4000);
}
