function checkPasswordStrength(password) {
    const criteria = {
        length:  password.length >= 8,
        upper:   /[A-Z]/.test(password),
        lower:   /[a-z]/.test(password),
        number:  /[0-9]/.test(password),
        special: /[@$!%*?&\-_#^()+={}\[\]|:;<>,.?/~`"']/.test(password)
    };

    const score = Object.values(criteria).filter(Boolean).length;

    // Cập nhật checklist
    const map = {
        'check-length':  criteria.length,
        'check-upper':   criteria.upper,
        'check-lower':   criteria.lower,
        'check-number':  criteria.number,
        'check-special': criteria.special
    };
    for (const [id, passed] of Object.entries(map)) {
        const el = document.getElementById(id);
        if (el) {
            if (passed) el.classList.add('valid');
            else        el.classList.remove('valid');
        }
    }

    // Màu thanh điểm & nhãn
    const colors = ['', '#ef4444', '#f97316', '#eab308', '#84cc16', '#16a34a'];
    const labels = ['', 'Rất yếu', 'Yếu', 'Trung bình', 'Mạnh', 'Rất mạnh'];
    for (let i = 1; i <= 5; i++) {
        const bar = document.getElementById('bar-' + i);
        if (bar) bar.style.backgroundColor = i <= score ? colors[score] : '#e5e7eb';
    }
    const labelEl = document.getElementById('strength-label');
    if (labelEl) {
        labelEl.textContent = score > 0 ? labels[score] : '';
        labelEl.style.color = colors[score] || '#888';
    }
}

document.addEventListener("DOMContentLoaded", () => {
    // Lấy ô password (hỗ trợ cả id='password' hoặc id='newPassword')
    const passwordField = document.getElementById("password") || document.getElementById("newPassword");
    
    // Hiện box và gọi checker mỗi khi người dùng nhập password
    if (passwordField) {
        passwordField.addEventListener('input', function() {
            const box = document.getElementById('password-strength-box');
            if (box) {
                if (this.value.length > 0) {
                    box.style.display = 'block';
                    checkPasswordStrength(this.value);
                } else {
                    box.style.display = 'none';
                }
            }
        });
    }
});
