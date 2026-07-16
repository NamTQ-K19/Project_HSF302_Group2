-- ============================================================
-- SẢN PHẨM CHO DANH MỤC TRÀ VÀ TRÀ SỮA
-- product_id từ 7 đến 12
-- ============================================================

-- Sản phẩm 7: Trà Đào Cam Sả (product_id = 7)
INSERT INTO products (category_id, name, description, is_active) VALUES
    (2, N'Trà Đào Cam Sả', N'Trà đào cam sả thanh mát với những lát đào tươi, cam vàng và sả nồng nàn', 1);

-- Sản phẩm 8: Trà Ổi Hồng (product_id = 8)
INSERT INTO products (category_id, name, description, is_active) VALUES
    (2, N'Trà Ổi Hồng', N'Trà ổi hồng chua ngọt thanh nhẹ, giải nhiệt mùa hè', 1);

-- Sản phẩm 9: Trà Xoài Chanh Dây (product_id = 9)
INSERT INTO products (category_id, name, description, is_active) VALUES
    (2, N'Trà Xoài Chanh Dây', N'Sự hòa quyện tuyệt vời giữa mứt xoài ngọt lịm và chanh dây chua thanh', 1);

-- Sản phẩm 10: Trà Sữa Trân Châu Đường Đen (product_id = 10)
INSERT INTO products (category_id, name, description, is_active) VALUES
    (2, N'Trà Sữa Trân Châu Đường Đen', N'Trà sữa béo ngậy kết hợp cùng trân châu đường đen dẻo dai đậm vị', 1);

-- Sản phẩm 11: Trà Sữa Truyền Thống (product_id = 11)
INSERT INTO products (category_id, name, description, is_active) VALUES
    (2, N'Trà Sữa Truyền Thống', N'Hương vị trà sữa nguyên bản, sự kết hợp hoàn hảo giữa hồng trà và sữa', 1);

-- Sản phẩm 12: Trà Thái Xanh (product_id = 12)
INSERT INTO products (category_id, name, description, is_active) VALUES
    (2, N'Trà Thái Xanh', N'Trà sữa Thái xanh thơm mát đặc trưng, màu sắc bắt mắt', 1);


-- ============================================================
-- BIẾN THỂ CHO TRÀ ĐÀO CAM SẢ (product_id = 7)
-- variant_id bắt đầu từ 19
-- ============================================================
INSERT INTO product_variants (product_id, variant_name, size, price, temperature, is_available) VALUES
                                                                                                    (7, N'Trà Đào Cam Sả Size S', 'S', 35000, 'COLD', 1),  -- variant_id = 19
                                                                                                    (7, N'Trà Đào Cam Sả Size M', 'M', 45000, 'COLD', 1),  -- variant_id = 20
                                                                                                    (7, N'Trà Đào Cam Sả Size L', 'L', 55000, 'COLD', 1);  -- variant_id = 21

-- ============================================================
-- BIẾN THỂ CHO TRÀ ỔI HỒNG (product_id = 8)
-- ============================================================
INSERT INTO product_variants (product_id, variant_name, size, price, temperature, is_available) VALUES
                                                                                                    (8, N'Trà Ổi Hồng Size S', 'S', 35000, 'COLD', 1),  -- variant_id = 22
                                                                                                    (8, N'Trà Ổi Hồng Size M', 'M', 45000, 'COLD', 1),  -- variant_id = 23
                                                                                                    (8, N'Trà Ổi Hồng Size L', 'L', 55000, 'COLD', 1);  -- variant_id = 24

-- ============================================================
-- BIẾN THỂ CHO TRÀ XOÀI CHANH DÂY (product_id = 9)
-- ============================================================
INSERT INTO product_variants (product_id, variant_name, size, price, temperature, is_available) VALUES
                                                                                                    (9, N'Trà Xoài Chanh Dây Size S', 'S', 39000, 'COLD', 1),  -- variant_id = 25
                                                                                                    (9, N'Trà Xoài Chanh Dây Size M', 'M', 49000, 'COLD', 1),  -- variant_id = 26
                                                                                                    (9, N'Trà Xoài Chanh Dây Size L', 'L', 59000, 'COLD', 1);  -- variant_id = 27

-- ============================================================
-- BIẾN THỂ CHO TRÀ SỮA TRÂN CHÂU ĐƯỜNG ĐEN (product_id = 10)
-- ============================================================
INSERT INTO product_variants (product_id, variant_name, size, price, temperature, is_available) VALUES
                                                                                                    (10, N'Trà Sữa Trân Châu Đường Đen Size S', 'S', 45000, 'COLD', 1),  -- variant_id = 28
                                                                                                    (10, N'Trà Sữa Trân Châu Đường Đen Size M', 'M', 55000, 'COLD', 1),  -- variant_id = 29
                                                                                                    (10, N'Trà Sữa Trân Châu Đường Đen Size L', 'L', 65000, 'COLD', 1);  -- variant_id = 30

-- ============================================================
-- BIẾN THỂ CHO TRÀ SỮA TRUYỀN THỐNG (product_id = 11)
-- ============================================================
INSERT INTO product_variants (product_id, variant_name, size, price, temperature, is_available) VALUES
                                                                                                    (11, N'Trà Sữa Truyền Thống Size S', 'S', 35000, 'COLD', 1),  -- variant_id = 31
                                                                                                    (11, N'Trà Sữa Truyền Thống Size M', 'M', 45000, 'COLD', 1),  -- variant_id = 32
                                                                                                    (11, N'Trà Sữa Truyền Thống Size L', 'L', 55000, 'COLD', 1);  -- variant_id = 33

-- ============================================================
-- BIẾN THỂ CHO TRÀ THÁI XANH (product_id = 12)
-- ============================================================
INSERT INTO product_variants (product_id, variant_name, size, price, temperature, is_available) VALUES
                                                                                                    (12, N'Trà Thái Xanh Size S', 'S', 35000, 'COLD', 1),  -- variant_id = 34
                                                                                                    (12, N'Trà Thái Xanh Size M', 'M', 45000, 'COLD', 1),  -- variant_id = 35
                                                                                                    (12, N'Trà Thái Xanh Size L', 'L', 55000, 'COLD', 1);  -- variant_id = 36


-- ============================================================
-- HÌNH ẢNH CHO TRÀ ĐÀO CAM SẢ (product_id = 7)
-- ============================================================
INSERT INTO product_images (product_id, image_url, is_primary, variant_id) VALUES
                                                                               (7, 'images/products/tradaocamxaS.jpg', 1, 19),
                                                                               (7, 'images/products/tradaocamsaM.jpg', 0, 20),
                                                                               (7, 'images/products/tradaocamXaL.jpg', 0, 21);

-- ============================================================
-- HÌNH ẢNH CHO TRÀ ỔI HỒNG (product_id = 8)
-- ============================================================
INSERT INTO product_images (product_id, image_url, is_primary, variant_id) VALUES
                                                                               (8, 'images/products/traoihongs.webp', 1, 22),
                                                                               (8, 'images/products/traoihongM.jpg', 0, 23),
                                                                               (8, 'images/products/traoihongsizeL.webp', 0, 24);

-- ============================================================
-- HÌNH ẢNH CHO TRÀ XOÀI CHANH DÂY (product_id = 9)
-- ============================================================
INSERT INTO product_images (product_id, image_url, is_primary, variant_id) VALUES
                                                                               (9, 'images/products/trasoaichanhdaysizeS.jpg', 1, 25),
                                                                               (9, 'images/products/trasoaichanhdaysizeM.jpg', 0, 26),
                                                                               (9, 'images/products/trasoaichanhdaysizeL.jpg', 0, 27);

-- ============================================================
-- HÌNH ẢNH CHO TRÀ SỮA TRÂN CHÂU ĐƯỜNG ĐEN (product_id = 10)
-- ============================================================
INSERT INTO product_images (product_id, image_url, is_primary, variant_id) VALUES
                                                                               (10, 'images/products/trasuachanchauduongdenS.webp', 1, 28),
                                                                               (10, 'images/products/trasuatranchauduongdenM.jpg', 0, 29),
                                                                               (10, 'images/products/trasuachanchauduongdenL.jpg', 0, 30);

-- ============================================================
-- HÌNH ẢNH CHO TRÀ SỮA TRUYỀN THỐNG (product_id = 11)
-- ============================================================
INSERT INTO product_images (product_id, image_url, is_primary, variant_id) VALUES
                                                                               (11, 'images/products/trasuatruyenthongsizes.webp', 1, 31),
                                                                               (11, 'images/products/trasuatruyenthongM.jpg', 0, 32),
                                                                               (11, 'images/products/trasuatruyenthongsizeL.webp', 0, 33);

-- ============================================================
-- HÌNH ẢNH CHO TRÀ THÁI XANH (product_id = 12)
-- ============================================================
INSERT INTO product_images (product_id, image_url, is_primary, variant_id) VALUES
                                                                               (12, 'images/products/trathaixanhS.jpg', 1, 34),
                                                                               (12, 'images/products/trathaixxanhM.webp', 0, 35),
                                                                               (12, 'images/products/trathaixanhL.jpg', 0, 36);