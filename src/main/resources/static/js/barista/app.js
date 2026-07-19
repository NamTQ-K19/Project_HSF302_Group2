function updateClock() {
    const now = new Date();
    document.getElementById('live-clock').innerText = now.toLocaleTimeString('vi-VN', {hour: '2-digit', minute: '2-digit'});
}
setInterval(updateClock, 1000);
updateClock();

// Modal Logic
function openReportModal(itemId, itemName) {
    document.getElementById('modal-item-id').value = itemId;
    document.getElementById('modal-item-name').innerText = itemName;
    document.querySelectorAll('.reason-btn').forEach(b => b.classList.remove('selected'));
    document.getElementById('btn-submit-report').disabled = true;
    document.getElementById('report-modal').classList.add('active');
}

function closeModal() {
    document.getElementById('report-modal').classList.remove('active');
    document.getElementById('modal-item-id').value = "";
    document.getElementById('modal-cancel-reason').value = "";
    document.getElementById('custom-reason-container').style.display = 'none';
    document.getElementById('custom-reason-input').value = "";
}

function selectReason(reason) {
    document.querySelectorAll('.reason-btn').forEach(b => {
        let text = b.innerText.trim();
        if (text === reason || text === reason + '...') {
            b.classList.add('selected');
        } else {
            b.classList.remove('selected');
        }
    });

    if (reason === 'Khác') {
        document.getElementById('custom-reason-container').style.display = 'block';
        document.getElementById('custom-reason-input').focus();
        document.getElementById('modal-cancel-reason').value = document.getElementById('custom-reason-input').value;
        // Chưa nhập gì thì KHÔNG bật nút submit — chờ updateCustomReason() xử lý khi có nội dung
        document.getElementById('btn-submit-report').disabled = true;
    } else {
        document.getElementById('custom-reason-container').style.display = 'none';
        document.getElementById('modal-cancel-reason').value = reason;
        // Các lý do dựng sẵn luôn có nội dung -> bật nút submit ngay
        document.getElementById('btn-submit-report').disabled = false;
    }
}

function updateCustomReason() {
    const value = document.getElementById('custom-reason-input').value.trim();
    document.getElementById('modal-cancel-reason').value = value;
    // Chỉ bật nút submit khi ô nhập thực sự có nội dung
    document.getElementById('btn-submit-report').disabled = (value.length === 0);
}
