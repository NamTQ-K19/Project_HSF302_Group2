// src/main/resources/static/js/customer.js

// ⭐ Đã xóa CSRF token setup
function formatCurrency(amount) {
    return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND',
        minimumFractionDigits: 0,
        maximumFractionDigits: 0
    }).format(amount);
}

function showLoading() {
    if ($('#loadingOverlay').length === 0) {
        $('body').append(`
            <div id="loadingOverlay" class="spinner-overlay">
                <div class="spinner-border text-primary" role="status" style="width: 3rem; height: 3rem;">
                    <span class="visually-hidden">Loading...</span>
                </div>
            </div>
        `);
    } else {
        $('#loadingOverlay').show();
    }
}

function hideLoading() {
    $('#loadingOverlay').hide();
}

function showToast(message, type = 'success') {
    if (window.showGlobalToast) {
        window.showGlobalToast(message, type);
    } else {
        alert(message);
    }
}

$(document).ready(function() {
    $('.alert').delay(5000).slideUp(300);
});