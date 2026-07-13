package hsf302.se2033jv.project_hsf302_group2.manager.service.impl;

import hsf302.se2033jv.project_hsf302_group2.common.entity.Order;
import hsf302.se2033jv.project_hsf302_group2.common.entity.OrderDetail;
import hsf302.se2033jv.project_hsf302_group2.common.enums.OrderStatus;
import hsf302.se2033jv.project_hsf302_group2.common.enums.OrderType;
import hsf302.se2033jv.project_hsf302_group2.common.repository.OrderRepository;
import hsf302.se2033jv.project_hsf302_group2.common.repository.ProductRepository;
import hsf302.se2033jv.project_hsf302_group2.manager.service.interfaces.IReportService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements IReportService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public ReportServiceImpl(OrderRepository orderRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    private Map<String, LocalDateTime> calculateDateRange(String period, String startDateStr, String endDateStr) {
        LocalDateTime start;
        LocalDateTime end = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);

        if ("today".equalsIgnoreCase(period)) {
            start = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        } else if ("week".equalsIgnoreCase(period)) {
            start = LocalDateTime.of(LocalDate.now().minusDays(7), LocalTime.MIN);
        } else if ("month".equalsIgnoreCase(period)) {
            start = LocalDateTime.of(LocalDate.now().minusDays(30), LocalTime.MIN);
        } else if ("custom".equalsIgnoreCase(period) && startDateStr != null && !startDateStr.isEmpty()) {
            try {
                LocalDate parseStart = LocalDate.parse(startDateStr);
                LocalDate parseEnd = (endDateStr != null && !endDateStr.isEmpty()) ? LocalDate.parse(endDateStr) : LocalDate.now();
                start = LocalDateTime.of(parseStart, LocalTime.MIN);
                end = LocalDateTime.of(parseEnd, LocalTime.MAX);
            } catch (Exception e) {
                // Fallback to month if invalid date
                start = LocalDateTime.of(LocalDate.now().minusDays(30), LocalTime.MIN);
            }
        } else {
            // Default: month
            start = LocalDateTime.of(LocalDate.now().minusDays(30), LocalTime.MIN);
        }

        Map<String, LocalDateTime> range = new HashMap<>();
        range.put("start", start);
        range.put("end", end);
        return range;
    }

    @Override
    public Map<String, Object> getReportData(String period, String startDateStr, String endDateStr) {
        Map<String, LocalDateTime> dateRange = calculateDateRange(period, startDateStr, endDateStr);
        LocalDateTime start = dateRange.get("start");
        LocalDateTime end = dateRange.get("end");

        // Fetch completed orders within range to perform calculations
        Pageable unpaged = Pageable.unpaged();
        Page<Order> completedOrdersPage = orderRepository.findWithDynamicFilter(
                null, null, OrderStatus.COMPLETED, null, null, start, end, unpaged
        );
        List<Order> completedOrders = completedOrdersPage.getContent();

        // 1. Sales Report Statistics
        BigDecimal totalRevenue = completedOrders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long completedCount = completedOrders.size();
        BigDecimal avgOrderValue = completedCount > 0 
                ? totalRevenue.divide(BigDecimal.valueOf(completedCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Daily trend logic
        Map<String, BigDecimal> dailyTrendMap = new TreeMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        // Initialize all dates with 0
        LocalDate current = start.toLocalDate();
        LocalDate lastDate = end.toLocalDate();
        while (!current.isAfter(lastDate)) {
            dailyTrendMap.put(current.format(formatter), BigDecimal.ZERO);
            current = current.plusDays(1);
        }
        
        // Fill active dates
        for (Order order : completedOrders) {
            String dateKey = order.getCreatedAt().format(formatter);
            dailyTrendMap.put(dateKey, dailyTrendMap.getOrDefault(dateKey, BigDecimal.ZERO).add(order.getTotalAmount()));
        }

        List<Map<String, Object>> salesTrend = new ArrayList<>();
        for (Map.Entry<String, BigDecimal> entry : dailyTrendMap.entrySet()) {
            Map<String, Object> point = new HashMap<>();
            point.put("date", entry.getKey());
            point.put("revenue", entry.getValue());
            salesTrend.add(point);
        }

        // Sales by Order Type
        Map<String, BigDecimal> revenueByTypeMap = completedOrders.stream()
                .collect(Collectors.groupingBy(
                        order -> order.getOrderType().name(),
                        Collectors.reducing(BigDecimal.ZERO, Order::getTotalAmount, BigDecimal::add)
                ));

        List<Map<String, Object>> salesByType = new ArrayList<>();
        for (OrderType type : OrderType.values()) {
            Map<String, Object> typeMap = new HashMap<>();
            typeMap.put("type", type.name());
            typeMap.put("revenue", revenueByTypeMap.getOrDefault(type.name(), BigDecimal.ZERO));
            salesByType.add(typeMap);
        }

        // 2. Order Report Statistics
        // Fetch all orders (regardless of status) in the period to show full breakdown
        Page<Order> allOrdersPage = orderRepository.findWithDynamicFilter(
                null, null, null, null, null, start, end, unpaged
        );
        List<Order> allOrders = allOrdersPage.getContent();
        long totalOrdersCount = allOrders.size();

        Map<String, Long> countByStatus = allOrders.stream()
                .collect(Collectors.groupingBy(order -> order.getOrderStatus().name(), Collectors.counting()));

        List<Map<String, Object>> ordersByStatus = new ArrayList<>();
        for (OrderStatus status : OrderStatus.values()) {
            Map<String, Object> statusMap = new HashMap<>();
            statusMap.put("status", status.name());
            statusMap.put("count", countByStatus.getOrDefault(status.name(), 0L));
            ordersByStatus.add(statusMap);
        }

        // Order Type breakdown for ALL orders
        Map<String, Long> countByType = allOrders.stream()
                .collect(Collectors.groupingBy(order -> order.getOrderType().name(), Collectors.counting()));

        List<Map<String, Object>> ordersByType = new ArrayList<>();
        for (OrderType type : OrderType.values()) {
            Map<String, Object> typeMap = new HashMap<>();
            typeMap.put("type", type.name());
            typeMap.put("count", countByType.getOrDefault(type.name(), 0L));
            ordersByType.add(typeMap);
        }

        // Recent Orders List (Max 10)
        List<Order> recentOrdersList = allOrders.stream()
                .sorted(Comparator.comparing(Order::getCreatedAt).reversed())
                .limit(10)
                .collect(Collectors.toList());

        // 3. Product Report Statistics
        // Aggregating product sales from COMPLETED orders
        Map<Integer, Map<String, Object>> productSalesMap = new HashMap<>();
        long totalItemsSold = 0;

        for (Order order : completedOrders) {
            if (order.getOrderDetails() != null) {
                for (OrderDetail detail : order.getOrderDetails()) {
                    Integer prodId = detail.getProduct().getProductId();
                    String prodName = detail.getProductNameSnapshot();
                    String catName = detail.getProduct().getCategory() != null 
                            ? detail.getProduct().getCategory().getName() 
                            : "Không phân loại";
                    int qty = detail.getQuantity() != null ? detail.getQuantity() : 0;
                    BigDecimal total = detail.getItemTotal() != null ? detail.getItemTotal() : BigDecimal.ZERO;

                    totalItemsSold += qty;

                    Map<String, Object> prodStats = productSalesMap.computeIfAbsent(prodId, k -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("productId", prodId);
                        map.put("productName", prodName);
                        map.put("categoryName", catName);
                        map.put("quantitySold", 0);
                        map.put("revenue", BigDecimal.ZERO);
                        return map;
                    });

                    prodStats.put("quantitySold", (int) prodStats.get("quantitySold") + qty);
                    prodStats.put("revenue", ((BigDecimal) prodStats.get("revenue")).add(total));
                }
            }
        }

        List<Map<String, Object>> productSalesList = new ArrayList<>(productSalesMap.values());
        // Sort by quantity sold desc
        productSalesList.sort((a, b) -> Integer.compare((int) b.get("quantitySold"), (int) a.get("quantitySold")));

        // Construct complete result
        Map<String, Object> result = new HashMap<>();
        result.put("period", period);
        result.put("startDate", start.toLocalDate().toString());
        result.put("endDate", end.toLocalDate().toString());

        // Sales section
        Map<String, Object> salesSection = new HashMap<>();
        salesSection.put("totalRevenue", totalRevenue);
        salesSection.put("totalCompletedOrders", completedCount);
        salesSection.put("avgOrderValue", avgOrderValue);
        salesSection.put("salesTrend", salesTrend);
        salesSection.put("salesByType", salesByType);
        result.put("sales", salesSection);

        // Orders section
        Map<String, Object> ordersSection = new HashMap<>();
        ordersSection.put("totalOrdersCount", totalOrdersCount);
        ordersSection.put("ordersByStatus", ordersByStatus);
        ordersSection.put("ordersByType", ordersByType);
        ordersSection.put("recentOrders", recentOrdersList);
        result.put("orders", ordersSection);

        // Products section
        Map<String, Object> productsSection = new HashMap<>();
        productsSection.put("totalItemsSold", totalItemsSold);
        productsSection.put("bestSellers", productSalesList.stream().limit(10).collect(Collectors.toList()));
        productsSection.put("allProductSales", productSalesList);
        result.put("products", productsSection);

        return result;
    }

    @Override
    public byte[] generateExcelReport(String period, String startDateStr, String endDateStr, String type) {
        Map<String, Object> reportData = getReportData(period, startDateStr, endDateStr);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        try (org.apache.poi.xssf.usermodel.XSSFWorkbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
             java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream()) {

            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("Report");

            org.apache.poi.ss.usermodel.CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(org.apache.poi.ss.usermodel.IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);

            // Title
            org.apache.poi.ss.usermodel.Row titleRow = sheet.createRow(0);
            org.apache.poi.ss.usermodel.Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("BÁO CÁO " + (type.equals("sales") ? "DOANH THU" : type.equals("orders") ? "ĐƠN HÀNG" : "SẢN PHẨM BÁN CHẠY"));
            titleCell.setCellStyle(headerStyle);

            org.apache.poi.ss.usermodel.Row periodRow = sheet.createRow(1);
            periodRow.createCell(0).setCellValue("Khoảng thời gian:");
            periodRow.createCell(1).setCellValue(reportData.get("startDate") + " đến " + reportData.get("endDate"));

            int rowNum = 3;

            if ("sales".equalsIgnoreCase(type)) {
                Map<String, Object> sales = (Map<String, Object>) reportData.get("sales");
                
                org.apache.poi.ss.usermodel.Row r = sheet.createRow(rowNum++);
                r.createCell(0).setCellValue("Tổng doanh thu:");
                r.createCell(1).setCellValue(sales.get("totalRevenue").toString() + " VND");
                
                r = sheet.createRow(rowNum++);
                r.createCell(0).setCellValue("Số đơn hàng hoàn thành:");
                r.createCell(1).setCellValue(sales.get("totalCompletedOrders").toString());
                
                r = sheet.createRow(rowNum++);
                r.createCell(0).setCellValue("Giá trị trung bình đơn:");
                r.createCell(1).setCellValue(sales.get("avgOrderValue").toString() + " VND");

                rowNum++;
                org.apache.poi.ss.usermodel.Row tbHeader = sheet.createRow(rowNum++);
                String[] headers = {"Ngày", "Doanh thu (VND)"};
                for (int i = 0; i < headers.length; i++) {
                    org.apache.poi.ss.usermodel.Cell c = tbHeader.createCell(i);
                    c.setCellValue(headers[i]);
                    c.setCellStyle(headerStyle);
                }

                List<Map<String, Object>> trend = (List<Map<String, Object>>) sales.get("salesTrend");
                if (trend != null) {
                    for (Map<String, Object> point : trend) {
                        org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowNum++);
                        row.createCell(0).setCellValue(point.get("date").toString());
                        row.createCell(1).setCellValue(Double.parseDouble(point.get("revenue").toString()));
                    }
                }
            } else if ("products".equalsIgnoreCase(type)) {
                Map<String, Object> products = (Map<String, Object>) reportData.get("products");
                
                org.apache.poi.ss.usermodel.Row r = sheet.createRow(rowNum++);
                r.createCell(0).setCellValue("Tổng số lượng sản phẩm đã bán:");
                r.createCell(1).setCellValue(products.get("totalItemsSold").toString());

                rowNum++;
                org.apache.poi.ss.usermodel.Row tbHeader = sheet.createRow(rowNum++);
                String[] headers = {"Mã sản phẩm", "Tên sản phẩm", "Danh mục", "Số lượng đã bán", "Doanh thu thu về (VND)"};
                for (int i = 0; i < headers.length; i++) {
                    org.apache.poi.ss.usermodel.Cell c = tbHeader.createCell(i);
                    c.setCellValue(headers[i]);
                    c.setCellStyle(headerStyle);
                }

                List<Map<String, Object>> items = (List<Map<String, Object>>) products.get("allProductSales");
                if (items != null) {
                    for (Map<String, Object> item : items) {
                        org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowNum++);
                        row.createCell(0).setCellValue(item.get("productId").toString());
                        row.createCell(1).setCellValue(item.get("productName").toString());
                        row.createCell(2).setCellValue(item.get("categoryName").toString());
                        row.createCell(3).setCellValue(Double.parseDouble(item.get("quantitySold").toString()));
                        row.createCell(4).setCellValue(Double.parseDouble(item.get("revenue").toString()));
                    }
                }
            } else {
                Map<String, Object> orders = (Map<String, Object>) reportData.get("orders");
                
                org.apache.poi.ss.usermodel.Row r = sheet.createRow(rowNum++);
                r.createCell(0).setCellValue("Tổng số đơn phát sinh:");
                r.createCell(1).setCellValue(orders.get("totalOrdersCount").toString());

                rowNum++;
                org.apache.poi.ss.usermodel.Row tbHeader = sheet.createRow(rowNum++);
                String[] headers = {"Mã đơn", "Khách hàng", "Loại đơn", "Trạng thái", "Tổng tiền (VND)", "Ngày tạo"};
                for (int i = 0; i < headers.length; i++) {
                    org.apache.poi.ss.usermodel.Cell c = tbHeader.createCell(i);
                    c.setCellValue(headers[i]);
                    c.setCellStyle(headerStyle);
                }

                List<Order> recent = (List<Order>) orders.get("recentOrders");
                if (recent != null) {
                    for (Order o : recent) {
                        org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowNum++);
                        String customerName = o.getUser() != null ? (o.getUser().getFirstName() + " " + o.getUser().getLastName()).trim() : "Khách vãng lai";
                        row.createCell(0).setCellValue(o.getOrderId());
                        row.createCell(1).setCellValue(customerName);
                        row.createCell(2).setCellValue(o.getOrderType().name());
                        row.createCell(3).setCellValue(o.getOrderStatus().name());
                        row.createCell(4).setCellValue(Double.parseDouble(o.getTotalAmount().toString()));
                        row.createCell(5).setCellValue(o.getCreatedAt().format(dtf));
                    }
                }
            }

            for (int i = 0; i < 6; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (java.io.IOException e) {
            throw new RuntimeException("Error generating Excel report", e);
        }
    }
}
