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
}

function selectReason(reason) {
    document.querySelectorAll('.reason-btn').forEach(b => {
        if (b.innerText.includes(reason)) b.classList.add('selected');
        else b.classList.remove('selected');
    });
    // Enable submit button
    document.getElementById('btn-submit-report').disabled = false;
}
