/**
 * BrewMaster Cashier POS Script
 * Handles product rendering, variant selection, cart management, and AJAX order checkout.
 */

let cart = [];
let currentProducts = [];
let selectedCustomerId = null;
let selectedTableId = null;
let currentCategoryId = null;
let selectedCustomerPoints = 0;

const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content') || 'X-CSRF-TOKEN';

function getHeaders() {
    const headers = { 'Content-Type': 'application/json' };
    if (csrfToken && csrfHeader) {
        headers[csrfHeader] = csrfToken;
    }
    return headers;
}

document.addEventListener('DOMContentLoaded', () => {
    loadProducts();
    loadTables();
    setupEventListeners();
    renderCart();
});

function setupEventListeners() {
    // Category filtering
    document.querySelectorAll('.category-tab').forEach(btn => {
        btn.addEventListener('click', (e) => {
            document.querySelectorAll('.category-tab').forEach(b => {
                b.classList.remove('bg-primary', 'text-on-primary', 'shadow-md');
                b.classList.add('bg-surface-container', 'text-on-surface-variant', 'hover:bg-surface-container-high');
            });
            btn.classList.remove('bg-surface-container', 'text-on-surface-variant', 'hover:bg-surface-container-high');
            btn.classList.add('bg-primary', 'text-on-primary', 'shadow-md');

            const catId = btn.getAttribute('data-category-id');
            currentCategoryId = catId ? parseInt(catId) : null;
            loadProducts();
        });
    });

    // Product search
    const searchInput = document.getElementById('product-search');
    let searchTimeout;
    if (searchInput) {
        searchInput.addEventListener('input', (e) => {
            clearTimeout(searchTimeout);
            searchTimeout = setTimeout(() => {
                loadProducts();
            }, 300);
        });
    }

    // Customer search
    const customerInput = document.getElementById('customer-search');
    const customerDropdown = document.getElementById('customer-dropdown');
    let custTimeout;
    if (customerInput) {
        customerInput.addEventListener('input', (e) => {
            clearTimeout(custTimeout);
            const kw = e.target.value.trim();
            if (!kw) {
                if (customerDropdown) customerDropdown.classList.add('hidden');
                return;
            }
            custTimeout = setTimeout(() => {
                fetch(`/order/api/customers?keyword=${encodeURIComponent(kw)}`, { headers: getHeaders() })
                    .then(res => res.json())
                    .then(users => {
                        renderCustomerDropdown(users);
                    })
                    .catch(err => console.error('Error searching customers:', err));
            }, 300);
        });

        customerInput.addEventListener('focus', () => {
            if (customerInput.value.trim() && customerDropdown && customerDropdown.children.length > 0) {
                customerDropdown.classList.remove('hidden');
            }
        });
    }

    // Hide dropdown when clicking outside
    document.addEventListener('click', (e) => {
        if (!e.target.closest('#customer-search-container') && customerDropdown) {
            customerDropdown.classList.add('hidden');
        }
    });
}

function loadProducts() {
    const kw = document.getElementById('product-search')?.value.trim() || '';
    let url = `/order/api/products?`;
    if (currentCategoryId) url += `categoryId=${currentCategoryId}&`;
    if (kw) url += `keyword=${encodeURIComponent(kw)}&`;

    const grid = document.getElementById('product-grid');
    if (grid) grid.innerHTML = `<div class="col-span-full py-12 text-center text-on-surface-variant"><span class="material-symbols-outlined text-4xl animate-spin">refresh</span><p class="mt-2 text-sm font-medium">Đang tải thực đơn...</p></div>`;

    fetch(url, { headers: getHeaders() })
        .then(res => res.json())
        .then(products => {
            currentProducts = products;
            renderProducts(products);
        })
        .catch(err => {
            console.error('Error loading products:', err);
            if (grid) grid.innerHTML = `<div class="col-span-full py-12 text-center text-error"><span class="material-symbols-outlined text-4xl">error</span><p class="mt-2 text-sm font-medium">Không thể tải danh sách sản phẩm!</p></div>`;
        });
}

function getCategoryIcon(name) {
    if (!name) return 'restaurant_menu';
    const lower = name.toLowerCase();
    if (lower.includes('cà phê') || lower.includes('coffee') || lower.includes('pha máy') || lower.includes('truyền thống')) return 'local_cafe';
    if (lower.includes('trà') || lower.includes('tea') || lower.includes('sữa') || lower.includes('trái cây')) return 'emoji_food_beverage';
    if (lower.includes('bánh') || lower.includes('cake') || lower.includes('bakery') || lower.includes('ăn vặt') || lower.includes('snack')) return 'bakery_dining';
    if (lower.includes('đá xay') || lower.includes('smoothie') || lower.includes('ice') || lower.includes('kem')) return 'icecream';
    if (lower.includes('nước ngọt') || lower.includes('chai') || lower.includes('lon') || lower.includes('soda') || lower.includes('nước giải khát')) return 'liquor';
    return 'restaurant_menu';
}

function decorateCategoryTabs() {
    document.querySelectorAll('.category-tab').forEach(btn => {
        const catId = btn.getAttribute('data-category-id');
        if (!catId) return;
        const nameEl = btn.querySelector('span:last-child') || btn;
        const name = nameEl.textContent.trim();
        const iconEl = btn.querySelector('.category-tab-icon');
        if (iconEl) {
            iconEl.textContent = getCategoryIcon(name);
        }
    });
}

function renderProductCardHtml(p) {
    const minPrice = p.variants && p.variants.length > 0 
        ? Math.min(...p.variants.map(v => v.price)) 
        : 0;
    const variantCount = p.variants ? p.variants.length : 0;
    const isMultiVariant = variantCount > 1;

    return `
        <div onclick="handleProductClick(${p.productId})" class="product-card bg-surface-container-lowest rounded-3xl p-3.5 border border-outline-variant/60 flex flex-col justify-between cursor-pointer group hover:border-primary/50 relative overflow-hidden transition-all duration-300 shadow-sm hover:shadow-xl">
            <div>
                <!-- Product Image -->
                <div class="aspect-square w-full rounded-2xl overflow-hidden bg-surface-container mb-3 relative shadow-inner">
                    <img src="${p.imageUrl}" alt="${p.name}" class="w-full h-full object-cover group-hover:scale-108 transition-transform duration-500 ease-out" onerror="this.src='/images/default-product.png'">
                    <div class="absolute inset-0 bg-gradient-to-t from-black/50 via-transparent to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-300"></div>
                    ${isMultiVariant ? `
                        <span class="absolute bottom-2.5 right-2.5 px-2.5 py-1 rounded-xl bg-black/75 backdrop-blur-md text-white text-[10px] font-bold flex items-center gap-1 shadow-md">
                            <span class="material-symbols-outlined text-[13px] text-amber-300">tune</span>
                            ${variantCount} loại
                        </span>
                    ` : `
                        <span class="absolute bottom-2.5 right-2.5 px-2.5 py-1 rounded-xl bg-emerald-600/85 backdrop-blur-md text-white text-[10px] font-bold flex items-center gap-1 shadow-md">
                            <span class="material-symbols-outlined text-[13px]">check</span>
                            Sẵn sàng
                        </span>
                    `}
                </div>

                <!-- Category & Title -->
                <div class="flex items-center justify-between gap-1 mb-1.5">
                    <span class="text-[10px] font-extrabold text-primary uppercase tracking-wider bg-primary/10 px-2.5 py-0.5 rounded-md line-clamp-1">${p.categoryName || 'Món ngon'}</span>
                </div>
                <h3 class="font-extrabold text-on-surface text-sm md:text-[15px] leading-snug line-clamp-1 group-hover:text-primary transition-colors mb-1">${p.name}</h3>
                <p class="text-[11px] text-on-surface-variant/80 line-clamp-1 m-0">${p.description || (isMultiVariant ? 'Nhiều tùy chọn size và giá' : 'Hương vị tuyệt hảo')}</p>
            </div>

            <!-- Price & Action Button -->
            <div class="mt-3.5 pt-3 border-t border-outline-variant/40 flex items-center justify-between gap-2">
                <div>
                    <span class="text-[10px] font-semibold text-on-surface-variant block leading-none mb-0.5">Giá từ</span>
                    <span class="font-extrabold text-primary text-base md:text-[17px] leading-none">${formatCurrency(minPrice)}</span>
                </div>
                <button class="px-3.5 py-2 rounded-xl ${isMultiVariant ? 'bg-secondary-container text-on-secondary-container hover:bg-primary hover:text-on-primary' : 'bg-primary text-on-primary hover:bg-primary/90'} font-bold text-xs flex items-center gap-1.5 shadow-sm transition-all border-none cursor-pointer group-hover:scale-105">
                    <span class="material-symbols-outlined text-[16px]">${isMultiVariant ? 'pageview' : 'add_shopping_cart'}</span>
                    <span>${isMultiVariant ? 'Chọn size' : 'Thêm'}</span>
                </button>
            </div>
        </div>
    `;
}

function slugifyCategoryName(name) {
    if (!name) return 'other';
    return name.toLowerCase()
        .normalize('NFD').replace(/[\u0300-\u036f]/g, '')
        .replace(/[^a-z0-9]+/g, '-')
        .replace(/^-+|-+$/g, '');
}

function scrollToPosCategory(catName) {
    const slug = slugifyCategoryName(catName);
    const el = document.getElementById(`pos-cat-${slug}`);
    if (el) {
        el.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }
}

function filterPosCategoryByName(catName) {
    const tabs = document.querySelectorAll('.category-tab');
    for (let btn of tabs) {
        const span = btn.querySelector('span:last-child') || btn;
        if (span.textContent.trim() === catName) {
            btn.click();
            return;
        }
    }
}

function renderProducts(products) {
    const grid = document.getElementById('product-grid');
    if (!grid) return;

    decorateCategoryTabs();

    if (!products || products.length === 0) {
        grid.innerHTML = `<div class="py-16 text-center text-on-surface-variant/60"><span class="material-symbols-outlined text-5xl">no_drinks</span><p class="mt-2 font-medium">Không tìm thấy sản phẩm nào</p></div>`;
        return;
    }

    let html = '';

    if (currentCategoryId) {
        // Specific category selected -> Hero Category Banner + items grid
        const activeTab = document.querySelector('.category-tab.bg-primary');
        const catName = activeTab ? activeTab.textContent.trim() : (products[0]?.categoryName || 'Danh mục');
        const icon = getCategoryIcon(catName);

        html += `
            <!-- Hero Category Banner -->
            <div class="mb-6">
                <div class="relative overflow-hidden rounded-3xl bg-gradient-to-br from-[#271310] via-[#3a1d18] to-[#5a2e26] text-white p-5 md:p-6 shadow-xl border border-white/10">
                    <div class="absolute -right-6 -bottom-6 opacity-15 pointer-events-none">
                        <span class="material-symbols-outlined text-[150px] text-amber-300">${icon}</span>
                    </div>
                    <div class="relative z-10 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                        <div class="flex items-center gap-4">
                            <div class="w-14 h-14 rounded-2xl bg-white/15 backdrop-blur-md flex items-center justify-center border border-white/20 shadow-inner shrink-0">
                                <span class="material-symbols-outlined text-[32px] text-amber-300">${icon}</span>
                            </div>
                            <div>
                                <span class="inline-block px-2.5 py-0.5 rounded-full bg-amber-400/20 text-amber-300 text-[10px] font-bold uppercase tracking-wider mb-1">Danh mục POS</span>
                                <h2 class="text-xl md:text-2xl font-extrabold m-0 text-white">${catName}</h2>
                                <p class="text-xs text-white/80 m-0 mt-0.5">Đang hiển thị tất cả món thuộc danh mục ${catName}</p>
                            </div>
                        </div>
                        <div class="flex items-center gap-2">
                            <button onclick="const allBtn = document.querySelector('.category-tab[data-category-id=\\'\\']'); if(allBtn) allBtn.click();" class="px-3.5 py-2 rounded-xl bg-white/10 hover:bg-white/20 text-white text-xs font-bold transition-all border border-white/20 cursor-pointer flex items-center gap-1.5">
                                <span class="material-symbols-outlined text-[16px]">grid_view</span>
                                <span>Xem tất cả danh mục</span>
                            </button>
                            <span class="px-3.5 py-2 rounded-xl bg-amber-400 text-[#271310] text-xs font-extrabold shadow-md">
                                ${products.length} món
                            </span>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Product Cards Grid -->
            <div class="pos-grid">
                ${products.map(p => renderProductCardHtml(p)).join('')}
            </div>
        `;
    } else {
        // "Tất cả" selected -> Structured Overview Dashboard Card + Self-contained Category Section Cards
        const grouped = {};
        products.forEach(p => {
            const catName = p.categoryName || 'Thực đơn khác';
            if (!grouped[catName]) grouped[catName] = [];
            grouped[catName].push(p);
        });

        const categoriesList = Object.keys(grouped);

        // Render each Category Section inside its own distinct card container
        categoriesList.forEach((catName) => {
            const items = grouped[catName];
            const icon = getCategoryIcon(catName);
            const slug = slugifyCategoryName(catName);

            html += `
                <div id="pos-cat-${slug}" class="bg-surface-container-lowest rounded-3xl p-5 md:p-6 border border-outline-variant/60 shadow-sm mb-6 last:mb-2 transition-all">
                    <!-- Section Header Bar -->
                    <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-3 pb-4 mb-5 border-b border-outline-variant/50">
                        <div class="flex items-center gap-3.5">
                            <div class="w-12 h-12 rounded-2xl bg-gradient-to-br from-primary to-primary/85 text-on-primary flex items-center justify-center shrink-0 shadow-md shadow-primary/25">
                                <span class="material-symbols-outlined text-[26px]">${icon}</span>
                            </div>
                            <div>
                                <div class="flex items-center gap-2.5">
                                    <h3 class="text-lg md:text-xl font-black text-on-surface m-0 tracking-tight">${catName}</h3>
                                    <span class="px-3 py-0.5 rounded-full bg-primary/10 text-primary text-xs font-extrabold">${items.length} món</span>
                                </div>
                                <p class="text-xs text-on-surface-variant/80 m-0 mt-0.5">Danh sách các món thuộc nhóm ${catName} sẵn sàng phục vụ</p>
                            </div>
                        </div>
                        <div class="flex items-center gap-2 self-start sm:self-auto">
                            <button onclick="filterPosCategoryByName('${catName.replace(/'/g, "\\'")}')" class="px-3.5 py-2 rounded-xl bg-surface-container hover:bg-primary hover:text-on-primary text-on-surface-variant hover:text-on-primary text-xs font-bold transition-all border border-outline-variant/60 cursor-pointer flex items-center gap-1.5">
                                <span>Lọc riêng danh mục</span>
                                <span class="material-symbols-outlined text-[16px]">arrow_forward</span>
                            </button>
                        </div>
                    </div>

                    <!-- Category Items Grid -->
                    <div class="pos-grid">
                        ${items.map(p => renderProductCardHtml(p)).join('')}
                    </div>
                </div>
            `;
        });
    }

    grid.innerHTML = html;
}

function handleProductClick(productId) {
    const product = currentProducts.find(p => p.productId === productId);
    if (!product || !product.variants || product.variants.length === 0) {
        if (typeof window.showGlobalToast === 'function') {
            window.showGlobalToast('Sản phẩm này hiện đang hết tùy chọn phục vụ', 'error');
        } else {
            alert('Sản phẩm này hiện đang hết tùy chọn phục vụ');
        }
        return;
    }

    if (product.variants.length === 1) {
        addToCart(product, product.variants[0], 1, '');
    } else {
        openVariantModal(product);
    }
}

function openVariantModal(product) {
    const modal = document.getElementById('variant-modal');
    const content = document.getElementById('variant-modal-content');
    if (!modal || !content) return;

    let selectedVariantId = product.variants[0].variantId;
    let qty = 1;

    content.innerHTML = `
        <div class="p-6">
            <div class="flex items-start gap-4 mb-6">
                <img src="${product.imageUrl}" class="w-20 h-20 rounded-xl object-cover bg-surface-container" onerror="this.src='/images/default-product.png'">
                <div class="flex-1">
                    <h3 class="text-lg font-bold text-on-surface m-0">${product.name}</h3>
                    <p class="text-xs text-on-surface-variant mt-1">${product.description || 'Chọn kích thước và hương vị phù hợp'}</p>
                    <div id="modal-price-display" class="font-headline-md text-primary font-bold text-lg mt-2">${formatCurrency(product.variants[0].price)}</div>
                </div>
                <button onclick="closeVariantModal()" class="w-8 h-8 rounded-full bg-surface-container flex items-center justify-center text-on-surface-variant hover:text-on-surface border-none cursor-pointer">
                    <span class="material-symbols-outlined text-[20px]">close</span>
                </button>
            </div>

            <div class="mb-6">
                <label class="block text-xs font-bold text-on-surface-variant uppercase tracking-wider mb-3">Tùy chọn Variant (Size / Nhiệt độ)</label>
                <div class="grid grid-cols-1 sm:grid-cols-2 gap-2.5">
                    ${product.variants.map((v, idx) => `
                        <div onclick="selectModalVariant(${v.variantId}, ${v.price})" id="var-btn-${v.variantId}" 
                             class="variant-option p-3 rounded-xl border ${idx === 0 ? 'border-primary bg-primary/10 font-bold text-primary' : 'border-outline-variant bg-surface-container-lowest text-on-surface'} cursor-pointer transition-all flex items-center justify-between">
                            <div>
                                <div class="text-sm">${v.variantName}</div>
                                <div class="text-[11px] text-on-surface-variant">${v.size || ''} ${v.temperature ? '• ' + v.temperature : ''}</div>
                            </div>
                            <div class="text-sm font-semibold">${formatCurrency(v.price)}</div>
                        </div>
                    `).join('')}
                </div>
            </div>

            <div class="mb-6">
                <label class="block text-xs font-bold text-on-surface-variant uppercase tracking-wider mb-2">Ghi chú đặc biệt (ít đường, nhiều đá...)</label>
                <input type="text" id="modal-special-note" placeholder="Nhập ghi chú cho pha chế..." class="w-full px-3 py-2.5 rounded-xl border border-outline-variant bg-surface-container-lowest text-sm text-on-surface focus:border-primary focus:outline-none">
            </div>

            <div class="flex items-center justify-between pt-4 border-t border-outline-variant/60">
                <div class="flex items-center border border-outline-variant rounded-xl bg-surface-container-lowest">
                    <button onclick="adjustModalQty(-1)" class="w-10 h-10 flex items-center justify-center text-on-surface font-bold text-lg border-none cursor-pointer bg-transparent hover:bg-surface-container">-</button>
                    <span id="modal-qty-val" class="w-10 text-center font-bold text-sm">1</span>
                    <button onclick="adjustModalQty(1)" class="w-10 h-10 flex items-center justify-center text-on-surface font-bold text-lg border-none cursor-pointer bg-transparent hover:bg-surface-container">+</button>
                </div>
                <button id="modal-add-btn" class="px-6 py-3 rounded-xl bg-primary text-on-primary font-bold text-sm shadow-lg shadow-primary/20 hover:bg-primary/90 transition-all border-none cursor-pointer flex items-center gap-2">
                    <span class="material-symbols-outlined text-[18px]">add_shopping_cart</span>
                    Thêm vào đơn
                </button>
            </div>
        </div>
    `;

    window.currentModalVariantId = selectedVariantId;
    window.currentModalQty = 1;

    window.selectModalVariant = (varId, price) => {
        window.currentModalVariantId = varId;
        document.querySelectorAll('.variant-option').forEach(el => {
            el.classList.remove('border-primary', 'bg-primary/10', 'font-bold', 'text-primary');
            el.classList.add('border-outline-variant', 'bg-surface-container-lowest', 'text-on-surface');
        });
        const activeEl = document.getElementById(`var-btn-${varId}`);
        if (activeEl) {
            activeEl.classList.remove('border-outline-variant', 'bg-surface-container-lowest', 'text-on-surface');
            activeEl.classList.add('border-primary', 'bg-primary/10', 'font-bold', 'text-primary');
        }
        const priceEl = document.getElementById('modal-price-display');
        if (priceEl) priceEl.innerText = formatCurrency(price * window.currentModalQty);
    };

    window.adjustModalQty = (delta) => {
        window.currentModalQty = Math.max(1, window.currentModalQty + delta);
        document.getElementById('modal-qty-val').innerText = window.currentModalQty;
        const v = product.variants.find(x => x.variantId === window.currentModalVariantId);
        if (v) {
            document.getElementById('modal-price-display').innerText = formatCurrency(v.price * window.currentModalQty);
        }
    };

    document.getElementById('modal-add-btn').onclick = () => {
        const v = product.variants.find(x => x.variantId === window.currentModalVariantId);
        const note = document.getElementById('modal-special-note').value.trim();
        if (v) {
            addToCart(product, v, window.currentModalQty, note);
            closeVariantModal();
        }
    };

    modal.classList.remove('hidden');
    modal.classList.add('flex');
}

function closeVariantModal() {
    const modal = document.getElementById('variant-modal');
    if (modal) {
        modal.classList.add('hidden');
        modal.classList.remove('flex');
    }
}

function addToCart(product, variant, quantity, note) {
    const existingIdx = cart.findIndex(item => item.variantId === variant.variantId && item.specialNote === note);
    if (existingIdx >= 0) {
        cart[existingIdx].quantity += quantity;
    } else {
        cart.push({
            productId: product.productId,
            productName: product.name,
            variantId: variant.variantId,
            variantName: variant.variantName,
            price: variant.price,
            quantity: quantity,
            specialNote: note || ''
        });
    }
    renderCart();
    if (typeof window.showGlobalToast === 'function') {
        window.showGlobalToast(`Đã thêm "${product.name}" vào đơn`, 'success');
    }
}

function removeFromCart(index) {
    cart.splice(index, 1);
    renderCart();
}

function adjustCartQty(index, delta) {
    cart[index].quantity += delta;
    if (cart[index].quantity <= 0) {
        removeFromCart(index);
    } else {
        renderCart();
    }
}

function renderCart() {
    const container = document.getElementById('cart-items-container');
    const badge = document.getElementById('cart-item-count');
    const subtotalEl = document.getElementById('cart-subtotal');
    const totalEl = document.getElementById('cart-total');
    const checkoutBtn = document.getElementById('btn-checkout');

    const totalQty = cart.reduce((sum, item) => sum + item.quantity, 0);
    const subtotal = cart.reduce((sum, item) => sum + (item.price * item.quantity), 0);

    let discountAmount = 0;
    const usePointsCb = document.getElementById('use-points-checkbox');
    const discountDisplay = document.getElementById('points-discount-display');
    const discountMoneyVal = document.getElementById('discount-money-val');
    const subtotalRow = document.getElementById('subtotal-row');

    if (usePointsCb && usePointsCb.checked && selectedCustomerId && selectedCustomerPoints > 0) {
        const maxDiscount = subtotal * 0.5;
        const maxPointsCanUse = Math.floor(maxDiscount / 100);
        const pointsToUse = Math.min(selectedCustomerPoints, maxPointsCanUse);
        if (pointsToUse > 0) {
            discountAmount = pointsToUse * 100;
            if (discountDisplay) {
                discountDisplay.classList.remove('hidden');
                discountDisplay.classList.add('flex');
            }
            if (discountMoneyVal) discountMoneyVal.innerText = '-' + formatCurrency(discountAmount);
            if (subtotalRow) {
                subtotalRow.classList.remove('hidden');
                subtotalRow.classList.add('flex');
            }
        } else {
            if (discountDisplay) {
                discountDisplay.classList.add('hidden');
                discountDisplay.classList.remove('flex');
            }
            if (subtotalRow) {
                subtotalRow.classList.add('hidden');
                subtotalRow.classList.remove('flex');
            }
        }
    } else {
        if (discountDisplay) {
            discountDisplay.classList.add('hidden');
            discountDisplay.classList.remove('flex');
        }
        if (subtotalRow) {
            subtotalRow.classList.add('hidden');
            subtotalRow.classList.remove('flex');
        }
    }

    const total = Math.max(0, subtotal - discountAmount);

    if (badge) badge.innerText = totalQty;
    if (subtotalEl) subtotalEl.innerText = formatCurrency(subtotal);
    if (totalEl) totalEl.innerText = formatCurrency(total);

    if (checkoutBtn) {
        if (cart.length === 0) {
            checkoutBtn.disabled = true;
            checkoutBtn.classList.add('opacity-50', 'cursor-not-allowed');
        } else {
            checkoutBtn.disabled = false;
            checkoutBtn.classList.remove('opacity-50', 'cursor-not-allowed');
        }
    }

    if (!container) return;

    if (cart.length === 0) {
        container.innerHTML = `
            <div class="h-full flex flex-col items-center justify-center py-16 text-on-surface-variant/50">
                <span class="material-symbols-outlined text-6xl mb-3">shopping_basket</span>
                <p class="font-medium text-sm m-0">Giỏ hàng đang trống</p>
                <p class="text-xs text-on-surface-variant/40 mt-1">Chọn sản phẩm từ danh sách bên trái</p>
            </div>
        `;
        return;
    }

    container.innerHTML = cart.map((item, idx) => `
        <div class="p-3.5 rounded-xl bg-surface-container-lowest border border-outline-variant/60 flex items-start justify-between gap-3 minimal-shadow mb-2.5">
            <div class="flex-1">
                <h4 class="font-bold text-on-surface text-sm m-0">${item.productName}</h4>
                <div class="text-xs text-primary font-medium mt-0.5">${item.variantName}</div>
                ${item.specialNote ? `<div class="text-[11px] text-on-surface-variant italic mt-1 bg-surface-container px-2 py-0.5 rounded inline-block">"${item.specialNote}"</div>` : ''}
                <div class="font-headline-md font-bold text-on-surface text-sm mt-2">${formatCurrency(item.price * item.quantity)}</div>
            </div>
            <div class="flex flex-col items-end gap-2">
                <button onclick="removeFromCart(${idx})" class="text-on-surface-variant/60 hover:text-error transition-colors border-none cursor-pointer bg-transparent p-0">
                    <span class="material-symbols-outlined text-[18px]">delete</span>
                </button>
                <div class="flex items-center border border-outline-variant rounded-lg bg-surface-container">
                    <button onclick="adjustCartQty(${idx}, -1)" class="w-7 h-7 flex items-center justify-center font-bold text-on-surface border-none cursor-pointer bg-transparent hover:bg-surface-container-high">-</button>
                    <span class="w-6 text-center font-bold text-xs">${item.quantity}</span>
                    <button onclick="adjustCartQty(${idx}, 1)" class="w-7 h-7 flex items-center justify-center font-bold text-on-surface border-none cursor-pointer bg-transparent hover:bg-surface-container-high">+</button>
                </div>
            </div>
        </div>
    `).join('');
}

function loadTables() {
    fetch('/order/api/tables', { headers: getHeaders() })
        .then(res => res.json())
        .then(tables => {
            renderTablesModal(tables);
        })
        .catch(err => console.error('Error loading tables:', err));
}

function renderTablesModal(tables) {
    const grid = document.getElementById('tables-grid');
    if (!grid) return;

    if (!tables || tables.length === 0) {
        grid.innerHTML = `<div class="col-span-full py-8 text-center text-on-surface-variant">Không có bàn nào trong hệ thống</div>`;
        return;
    }

    grid.innerHTML = `
        <div onclick="selectTable(null, 'Mang về (Takeaway)')" class="p-4 rounded-2xl border ${selectedTableId === null ? 'border-primary bg-primary/10 font-bold text-primary' : 'border-outline-variant bg-surface-container-lowest text-on-surface'} cursor-pointer transition-all flex flex-col items-center justify-center text-center">
            <span class="material-symbols-outlined text-3xl mb-1">takeout_dining</span>
            <div class="font-bold text-sm">Mang về</div>
            <div class="text-[11px] text-on-surface-variant mt-0.5">Takeaway order</div>
        </div>
        ${tables.map(t => {
            const isOcc = t.status === 'OCCUPIED';
            const isSel = selectedTableId === t.tableId;
            let bgClass = isSel ? 'border-primary bg-primary/10 text-primary font-bold shadow-md' : 'border-outline-variant bg-surface-container-lowest text-on-surface hover:border-primary/40';
            if (isOcc && !isSel) bgClass = 'border-amber-200 bg-amber-50 text-amber-900 opacity-80';

            return `
                <div onclick="selectTable(${t.tableId}, 'Bàn số ${t.tableId}')" class="p-4 rounded-2xl border ${bgClass} cursor-pointer transition-all flex flex-col items-center justify-center text-center relative">
                    <span class="material-symbols-outlined text-3xl mb-1 ${isOcc ? 'text-amber-600' : 'text-primary'}">table_restaurant</span>
                    <div class="font-bold text-sm">Bàn ${t.tableId}</div>
                    <div class="text-[11px] ${isOcc ? 'text-amber-700 font-semibold' : 'text-on-surface-variant'} mt-0.5">${isOcc ? 'Đang có khách' : (t.capacity + ' chỗ')}</div>
                </div>
            `;
        }).join('')}
    `;
}

function selectTable(tableId, label) {
    selectedTableId = tableId;
    const btnText = document.getElementById('selected-table-label');
    if (btnText) btnText.innerText = label;
    closeTableModal();
    loadTables(); // rerender classes
}

function openTableModal() {
    const modal = document.getElementById('table-modal');
    if (modal) {
        modal.classList.remove('hidden');
        modal.classList.add('flex');
    }
}

function closeTableModal() {
    const modal = document.getElementById('table-modal');
    if (modal) {
        modal.classList.add('hidden');
        modal.classList.remove('flex');
    }
}

function renderCustomerDropdown(users) {
    const dropdown = document.getElementById('customer-dropdown');
    if (!dropdown) return;

    if (!users || users.length === 0) {
        dropdown.innerHTML = `<div class="p-3 text-xs text-on-surface-variant text-center">Không tìm thấy khách hàng</div>`;
        dropdown.classList.remove('hidden');
        return;
    }

    dropdown.innerHTML = users.map(u => `
        <div onclick="selectCustomer(${u.userId}, '${u.fullName.replace(/'/g, "\\'")}', '${u.phone || ''}', ${u.loyaltyPoints || 0})" 
             class="p-2.5 hover:bg-surface-container rounded-lg cursor-pointer transition-colors flex items-center justify-between border-b border-outline-variant/40 last:border-none">
            <div>
                <div class="text-xs font-bold text-on-surface">${u.fullName || u.email}</div>
                <div class="text-[11px] text-on-surface-variant">${u.phone || u.email || ''} ${u.loyaltyPoints > 0 ? '• ★ ' + u.loyaltyPoints + ' pt' : ''}</div>
            </div>
            <span class="material-symbols-outlined text-primary text-[18px]">person_add</span>
        </div>
    `).join('');
    dropdown.classList.remove('hidden');
}

function selectCustomer(userId, name, phone, points) {
    selectedCustomerId = userId;
    selectedCustomerPoints = points || 0;
    const input = document.getElementById('customer-search');
    const badge = document.getElementById('selected-customer-badge');
    const dropdown = document.getElementById('customer-dropdown');
    const loyaltyArea = document.getElementById('loyalty-points-area');
    const pointsValEl = document.getElementById('customer-points-val');
    const pointsMoneyEl = document.getElementById('customer-points-money');
    const usePointsCb = document.getElementById('use-points-checkbox');

    if (input) input.value = '';
    if (dropdown) dropdown.classList.add('hidden');
    if (badge) {
        badge.innerHTML = `
            <div class="flex items-center justify-between bg-primary/10 border border-primary/20 px-3 py-2 rounded-xl text-primary font-medium text-xs">
                <div class="flex items-center gap-2">
                    <span class="material-symbols-outlined text-[18px]">person</span>
                    <span><strong>${name}</strong> ${phone ? '(' + phone + ')' : ''} ${selectedCustomerPoints > 0 ? '<span class="text-amber-700 ml-1 font-bold">★ ' + selectedCustomerPoints + ' pt</span>' : ''}</span>
                </div>
                <button onclick="removeCustomer()" class="text-primary hover:text-error transition-colors border-none cursor-pointer bg-transparent p-0 flex items-center">
                    <span class="material-symbols-outlined text-[16px]">close</span>
                </button>
            </div>
        `;
        badge.classList.remove('hidden');
    }

    if (loyaltyArea && selectedCustomerPoints > 0) {
        if (pointsValEl) pointsValEl.innerText = selectedCustomerPoints;
        if (pointsMoneyEl) pointsMoneyEl.innerText = `(~${formatCurrency(selectedCustomerPoints * 100)})`;
        if (usePointsCb) usePointsCb.checked = false;
        loyaltyArea.classList.remove('hidden');
    } else if (loyaltyArea) {
        loyaltyArea.classList.add('hidden');
    }
    renderCart();
}

function removeCustomer() {
    selectedCustomerId = null;
    selectedCustomerPoints = 0;
    const badge = document.getElementById('selected-customer-badge');
    const loyaltyArea = document.getElementById('loyalty-points-area');
    const usePointsCb = document.getElementById('use-points-checkbox');
    if (badge) badge.classList.add('hidden');
    if (loyaltyArea) loyaltyArea.classList.add('hidden');
    if (usePointsCb) usePointsCb.checked = false;
    renderCart();
}

function submitOrder() {
    if (cart.length === 0) {
        if (typeof window.showGlobalToast === 'function') {
            window.showGlobalToast('Vui lòng thêm ít nhất 1 món vào giỏ hàng', 'error');
        } else {
            alert('Vui lòng thêm ít nhất 1 món vào giỏ hàng');
        }
        return;
    }

    const total = cart.reduce((sum, item) => sum + (item.price * item.quantity), 0);
    const paymentMethodId = parseInt(document.getElementById('payment-method-select')?.value || '0');
    const isPaid = document.getElementById('is-paid-checkbox')?.checked || false;
    const note = document.getElementById('order-note')?.value.trim() || '';

    const executeCheckout = () => {
        const payload = {
            customerId: selectedCustomerId,
            tableId: selectedTableId,
            orderType: selectedTableId ? 'COUNTER' : 'ONLINE', // Takeaway can be ONLINE or COUNTER, enum OrderType has COUNTER / ONLINE
            note: note,
            paymentMethodId: paymentMethodId > 0 ? paymentMethodId : null,
            isPaidImmediately: isPaid,
            usePoints: document.getElementById('use-points-checkbox')?.checked || false,
            items: cart.map(i => ({
                productId: i.productId,
                variantId: i.variantId,
                quantity: i.quantity,
                specialNote: i.specialNote
            }))
        };

        const btn = document.getElementById('btn-checkout');
        if (btn) {
            btn.disabled = true;
            btn.innerHTML = `<span class="material-symbols-outlined animate-spin text-[20px]">refresh</span> Đang xử lý...`;
        }

        fetch('/order/api/create', {
            method: 'POST',
            headers: getHeaders(),
            body: JSON.stringify(payload)
        })
        .then(res => {
            if (!res.ok) {
                return res.json().then(err => { throw new Error(err.message || 'Lỗi đặt đơn hàng'); });
            }
            return res.json();
        })
        .then(order => {
            if (typeof window.showGlobalToast === 'function') {
                window.showGlobalToast(`Tạo đơn hàng thành công! Mã đơn: #ORD-${order.orderId}`, 'success');
            } else {
                alert(`Tạo đơn hàng thành công! Mã đơn: #ORD-${order.orderId}`);
            }
            // Clear cart & reset
            cart = [];
            selectedCustomerId = null;
            removeCustomer();
            selectTable(null, 'Mang về (Takeaway)');
            if (document.getElementById('order-note')) document.getElementById('order-note').value = '';
            if (document.getElementById('is-paid-checkbox')) document.getElementById('is-paid-checkbox').checked = true;
            renderCart();
            if (btn) {
                btn.disabled = false;
                btn.innerHTML = `<span class="material-symbols-outlined text-[20px]">check_circle</span> Thanh toán & Đặt đơn`;
            }
        })
        .catch(err => {
            console.error('Checkout error:', err);
            if (typeof window.showGlobalToast === 'function') {
                window.showGlobalToast(err.message || 'Có lỗi xảy ra khi tạo đơn hàng!', 'error');
            } else {
                alert(err.message || 'Có lỗi xảy ra khi tạo đơn hàng!');
            }
            if (btn) {
                btn.disabled = false;
                btn.innerHTML = `<span class="material-symbols-outlined text-[20px]">check_circle</span> Thanh toán & Đặt đơn`;
            }
        });
    };

    if (typeof window.showGlobalConfirm === 'function') {
        window.showGlobalConfirm(
            'Xác nhận tạo đơn POS',
            `Bạn muốn đặt đơn hàng trị giá <b>${formatCurrency(total)}</b>?<br>${isPaid ? '<span class="text-emerald-600 font-semibold">Đã thanh toán ngay tại quầy</span>' : '<span class="text-amber-600 font-semibold">Thanh toán sau (Pending)</span>'}`,
            executeCheckout
        );
    } else if (typeof Swal !== 'undefined') {
        Swal.fire({
            title: 'Xác nhận tạo đơn POS',
            html: `Bạn muốn đặt đơn hàng trị giá <b>${formatCurrency(total)}</b>?<br>${isPaid ? '<span class="text-emerald-600 font-semibold">Đã thanh toán ngay tại quầy</span>' : '<span class="text-amber-600 font-semibold">Thanh toán sau (Pending)</span>'}`,
            icon: 'question',
            showCancelButton: true,
            confirmButtonColor: '#271310',
            cancelButtonColor: '#706d6c',
            confirmButtonText: 'Đồng ý đặt đơn',
            cancelButtonText: 'Hủy'
        }).then((result) => {
            if (result.isConfirmed) executeCheckout();
        });
    } else {
        if (confirm(`Xác nhận tạo đơn hàng trị giá ${formatCurrency(total)}?`)) {
            executeCheckout();
        }
    }
}

function formatCurrency(val) {
    if (val === null || val === undefined) return '0 ₫';
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val);
}
