function buyNow() {
    const selectedRadio = document.querySelector('input[name="selectedVariant"]:checked');
    if (!selectedRadio) {
        alert('Vui lòng chọn kích cỡ!');
        return;
    }
    // Thêm vào giỏ rồi redirect thẳng sang trang giỏ hàng
    const variantId = selectedRadio.value;
    const qty = parseInt(document.getElementById('quantity')?.value, 10) || 1;
    const note = document.getElementById('specialNote')?.value || '';

    fetch('/cart/add', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            productId: productId,
            variantId: parseInt(variantId),
            quantity: qty,
            specialNote: note
        })
    }).then(res => {
        if (res.ok) {
            window.location.href = '/cart';  // redirect sang giỏ hàng
        } else if (res.status === 401) {
            window.location.href = '/auth/login';
        }
    });
}