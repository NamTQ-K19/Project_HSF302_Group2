document.addEventListener('DOMContentLoaded', function () {
    const btn = document.getElementById('btnConfirmCancel');
    if (btn) {
        btn.addEventListener('click', function (e) {
            const ok = confirm('Bạn có chắc chắn muốn hủy đặt bàn này không?\nHành động này không thể hoàn tác.');
            if (!ok) e.preventDefault();
        });
    }
});

