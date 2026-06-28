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
    const colors = {
        success: '#28a745',
        error: '#dc3545',
        warning: '#ffc107',
        info: '#17a2b8'
    };

    const toast = $(`
        <div class="toast align-items-center text-white border-0" role="alert" 
             style="position: fixed; top: 20px; right: 20px; z-index: 9999; min-width: 300px; background-color: ${colors[type]};">
            <div class="d-flex">
                <div class="toast-body">${message}</div>
                <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
            </div>
        </div>
    `);

    $('body').append(toast);
    const bsToast = new bootstrap.Toast(toast[0], { delay: 3000 });
    bsToast.show();

    toast.on('hidden.bs.toast', function() {
        $(this).remove();
    });
}

$(document).ready(function() {
    $('.alert').delay(5000).slideUp(300);
});