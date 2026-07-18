function updatePrice(radio) {
    const price = parseFloat(radio.dataset.price) || 0;
    const formatted = price.toLocaleString('vi-VN') + ' ₫';
    const display = document.getElementById('selectedPrice');
    if (display) display.textContent = formatted;

    document.querySelectorAll('.variant-option').forEach(function(opt) {
        opt.classList.remove('selected');
    });
    const label = radio.closest('.variant-option');
    if (label) label.classList.add('selected');

    const imageUrl = radio.dataset.image;
    if (imageUrl && imageUrl !== '/img/products/placeholder.jpg') {
        const mainImage = document.getElementById('mainImage');
        if (mainImage) {
            mainImage.src = imageUrl;
            
            let thumbs = document.querySelectorAll('.thumbnail-list img, .flex.gap-4.overflow-x-auto img');
            thumbs.forEach(t => {
                t.classList.remove('border-primary');
                t.classList.add('border-transparent');
                
                if (t.src.endsWith(imageUrl) || t.getAttribute('src') === imageUrl) {
                    t.classList.remove('border-transparent');
                    t.classList.add('border-primary');
                }
            });
        }
    }
}


function validateQty(input) {
    let val = parseInt(input.value, 10);
    if (isNaN(val) || val < 1) input.value = 1;
    else if (val > 99)         input.value = 99;
}

function changeQty(delta) {
    const input = document.getElementById('quantity');
    if (!input) return;
    const current = parseInt(input.value, 10) || 1;
    input.value = Math.max(1, Math.min(99, current + delta));
}
function notifyUser(msg, type = 'warning') {
    if (typeof window.showGlobalToast === 'function') {
        window.showGlobalToast(msg, type);
    } else {
        alert(msg);
    }
}

function buyNow() {
    const selectedRadio = document.querySelector('input[name="selectedVariant"]:checked');
    if (!selectedRadio) {
        notifyUser('Vui lòng chọn kích cỡ!', 'warning');
        return;
    }
    const variantId = selectedRadio.value;
    const qty = parseInt(document.getElementById('quantity')?.value, 10) || 1;
    const note = document.getElementById('specialNote')?.value || '';

    let headers = { 'Content-Type': 'application/json' };
    let csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
    let csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');
    if (csrfHeader && csrfToken) {
        headers[csrfHeader] = csrfToken;
    }

    fetch('/customer/cart/add', {
        method: 'POST',
        headers: headers,
        body: JSON.stringify({
            productId: productId,
            variantId: parseInt(variantId),
            quantity: qty,
            specialNote: note
        })
    }).then(res => {
        if (res.ok) {
            window.location.href = '/customer/cart';
        } else if (res.status === 401 || res.status === 403) {
            window.location.href = '/auth/login';
        } else {
            notifyUser('Lỗi khi thêm vào giỏ hàng', 'error');
        }
    });
}

function addToCart() {
    const selectedRadio = document.querySelector('input[name="selectedVariant"]:checked');
    if (!selectedRadio) {
        notifyUser('Vui lòng chọn kích cỡ!', 'warning');
        return;
    }
    const variantId = selectedRadio.value;
    const qty = parseInt(document.getElementById('quantity')?.value, 10) || 1;
    const note = document.getElementById('specialNote')?.value || '';

    let headers = { 'Content-Type': 'application/json' };
    let csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
    let csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');
    if (csrfHeader && csrfToken) {
        headers[csrfHeader] = csrfToken;
    }

    fetch('/customer/cart/add', {
        method: 'POST',
        headers: headers,
        body: JSON.stringify({
            productId: productId,
            variantId: parseInt(variantId),
            quantity: qty,
            specialNote: note
        })
    }).then(async res => {
        if (res.ok) {
            const data = await res.json();
            notifyUser('Đã thêm vào giỏ hàng!', 'success');
            if (data.data && data.data.totalItems !== undefined) {
                const badge = document.getElementById('cartCount');
                if (badge) badge.innerText = data.data.totalItems;
            }
        } else if (res.status === 401 || res.status === 403) {
            window.location.href = '/auth/login';
        } else {
            const data = await res.json();
            notifyUser('Lỗi: ' + (data.message || 'Không thể thêm vào giỏ hàng'), 'error');
        }
    }).catch(err => {
        console.error(err);
        notifyUser('Có lỗi xảy ra khi thêm vào giỏ hàng', 'error');
    });
}