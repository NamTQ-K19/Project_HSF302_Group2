// Cashier Reservation JavaScript

function openReservationDetailModal(reservationId) {
    const modal = document.getElementById('detail-modal');
    const contentContainer = document.getElementById('detail-modal-content');
    
    // Clear previous content and show loader/spinner
    contentContainer.innerHTML = `
        <div class="flex items-center justify-center p-16">
            <div class="w-8 h-8 rounded-full border-4 border-primary/20 border-t-primary animate-spin"></div>
        </div>
    `;
    modal.classList.remove('hidden');
    modal.classList.add('flex');

    // Fetch detail fragment via AJAX
    fetch(`/cashier/reservations/detail/${reservationId}`)
        .then(response => {
            if (!response.ok) {
                throw new Error("Không thể tải chi tiết đặt bàn");
            }
            return response.text();
        })
        .then(html => {
            contentContainer.innerHTML = html;
        })
        .catch(error => {
            contentContainer.innerHTML = `
                <div class="p-6 border-b border-outline-variant/40 flex justify-between items-center bg-surface-container/20">
                    <h3 class="text-lg font-bold text-on-surface m-0">Lỗi</h3>
                    <button onclick="closeModal()" class="w-8 h-8 rounded-full bg-surface-container hover:bg-surface-container-high flex items-center justify-center border-none cursor-pointer">
                        <span class="material-symbols-outlined text-[20px]">close</span>
                    </button>
                </div>
                <div class="p-8 text-center text-error font-medium">
                    ${error.message}
                </div>
            `;
        });
}

function closeModal() {
    const modal = document.getElementById('detail-modal');
    modal.classList.add('hidden');
    modal.classList.remove('flex');
}

// Close modal when clicking outside of the content container
document.getElementById('detail-modal').addEventListener('click', function(e) {
    if (e.target === this) {
        closeModal();
    }
});

function submitRefund(event) {
    event.preventDefault();
    
    const reservationId = document.getElementById('refund-reservation-id').value;
    const amount = document.getElementById('refund-amount').value;
    const note = document.getElementById('refund-note').value;
    const submitBtn = document.getElementById('submit-refund-btn');

    if (!amount || amount <= 0) {
        alert("Vui lòng nhập số tiền hoàn hợp lệ!");
        return;
    }
    if (!note || note.trim().isEmpty) {
        alert("Vui lòng nhập lý do hoàn tiền!");
        return;
    }

    // Disable button and show spinner
    const originalText = submitBtn.innerHTML;
    submitBtn.disabled = true;
    submitBtn.innerHTML = `
        <span class="w-4 h-4 rounded-full border-2 border-on-primary/20 border-t-on-primary animate-spin mr-1"></span>
        Đang xử lý...
    `;

    // Fetch CSRF Token
    const csrfToken = document.querySelector("meta[name='_csrf']")?.getAttribute("content");
    const csrfHeader = document.querySelector("meta[name='_csrf_header']")?.getAttribute("content");

    const headers = {
        'Content-Type': 'application/json'
    };
    if (csrfHeader && csrfToken) {
        headers[csrfHeader] = csrfToken;
    }

    fetch('/cashier/reservations/refund', {
        method: 'POST',
        headers: headers,
        body: JSON.stringify({
            reservationId: parseInt(reservationId),
            amount: parseFloat(amount),
            note: note
        })
    })
    .then(response => {
        if (!response.ok) {
            throw new Error("Lỗi kết nối máy chủ");
        }
        return response.json();
    })
    .then(data => {
        if (data.success) {
            alert(data.message);
            closeModal();
            // Reload the reservation list to show updated status
            window.location.reload();
        } else {
            alert("Hoàn tiền thất bại: " + data.message);
            submitBtn.disabled = false;
            submitBtn.innerHTML = originalText;
        }
    })
    .catch(error => {
        alert("Lỗi hệ thống: " + error.message);
        submitBtn.disabled = false;
        submitBtn.innerHTML = originalText;
    });
}
