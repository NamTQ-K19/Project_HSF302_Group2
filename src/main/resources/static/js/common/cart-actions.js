            // Load cart count on page load
            const updateGlobalCartBadge = async () => {
                try {
                    const res = await fetch('/customer/cart/json', {
                        headers: { 'Accept': 'application/json' }
                    });
                    if (res.ok) {
                        const data = await res.json();
                        if (data && data.totalItems !== undefined) {
                            const badge = document.getElementById('cartCount');
                            if (badge) badge.innerText = data.totalItems;
                        }
                    }
                } catch (e) {
                    console.error('Error fetching cart:', e);
                }
            };

            // Call it immediately
            document.addEventListener("DOMContentLoaded", updateGlobalCartBadge);

            // Use global window.showGlobalToast from toast.js

            function quickAddToCart(productId) {
                let headers = {
                    'Content-Type': 'application/json'
                };
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
                        quantity: 1
                    })
                })
                .then(async res => {
                    if (res.ok) {
                        const data = await res.json();
                        showGlobalToast('Đã thêm sản phẩm vào giỏ hàng!', 'success');
                        if (data.data && data.data.totalItems !== undefined) {
                            const badge = document.getElementById('cartCount');
                            if (badge) badge.innerText = data.data.totalItems;
                        }
                    } else if (res.status === 401 || res.status === 403) {
                        window.location.href = '/auth/login';
                    } else {
                        const data = await res.json();
                        showGlobalToast('Lỗi: ' + (data.message || 'Không thể thêm vào giỏ hàng'), 'error');
                    }
                })
                .catch(err => {
                    console.error(err);
                    showGlobalToast('Có lỗi xảy ra khi thêm vào giỏ hàng', 'error');
                });
            }
