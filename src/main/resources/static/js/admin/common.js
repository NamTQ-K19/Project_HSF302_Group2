// Hàm format currency dùng chung
function formatCurrency(amount) {
    if (!amount) return '0 đ';
    return new Intl.NumberFormat('vi-VN').format(amount) + ' đ';
}

// Toast notification dùng chung
function showToast(message, type) {
    const toast = document.getElementById('toast');
    const toastMessage = document.getElementById('toastMessage');
    const toastClose = document.getElementById('toastClose');

    if (!toast) return;

    toastMessage.textContent = message;
    toast.className = 'toast';
    toast.classList.add(type === 'success' ? 'toast-success' : 'toast-error');
    toast.style.display = 'flex';

    clearTimeout(toast._timeout);
    toast._timeout = setTimeout(() => {
        toast.style.display = 'none';
    }, 3000);
}

document.addEventListener('DOMContentLoaded', function() {
    // Close toast
    const toastClose = document.getElementById('toastClose');
    if (toastClose) {
        toastClose.addEventListener('click', function() {
            document.getElementById('toast').style.display = 'none';
        });
    }
});