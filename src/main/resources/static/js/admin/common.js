// Hàm format currency dùng chung
function formatCurrency(amount) {
    if (!amount) return '0 đ';
    return new Intl.NumberFormat('vi-VN').format(amount) + ' đ';
}

// Toast notification dùng chung
function showToast(message, type = 'success') {
    if (window.showGlobalToast) {
        window.showGlobalToast(message, type);
    } else {
        alert(message);
    }
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