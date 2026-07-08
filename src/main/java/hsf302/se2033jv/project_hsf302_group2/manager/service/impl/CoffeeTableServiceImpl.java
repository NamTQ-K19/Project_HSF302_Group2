package hsf302.se2033jv.project_hsf302_group2.manager.service.impl;

import hsf302.se2033jv.project_hsf302_group2.common.entity.CoffeeTable;
import hsf302.se2033jv.project_hsf302_group2.common.repository.CoffeeTableRepository;
import hsf302.se2033jv.project_hsf302_group2.common.exception.ResourceNotFoundException;
import hsf302.se2033jv.project_hsf302_group2.manager.dto.request.CoffeeTableRequest;
import hsf302.se2033jv.project_hsf302_group2.manager.dto.response.CoffeeTableResponse;
import hsf302.se2033jv.project_hsf302_group2.manager.service.interfaces.ICoffeeTableService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CoffeeTableServiceImpl implements ICoffeeTableService {

    private final CoffeeTableRepository tableRepository;

    public CoffeeTableServiceImpl(CoffeeTableRepository tableRepository) {
        this.tableRepository = tableRepository;
    }

    @Override
    public List<CoffeeTableResponse> getAllTables() {
        return tableRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CoffeeTableResponse createTable(CoffeeTableRequest dto) {
        CoffeeTable table = CoffeeTable.builder()
                .capacity(dto.getCapacity())
                .isActive(true)
                .build();
        return mapToResponse(tableRepository.save(table));
    }

    @Override
    @Transactional
    public CoffeeTableResponse updateTable(Integer id, CoffeeTableRequest dto) {
        CoffeeTable table = tableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bàn với ID: " + id));
        table.setCapacity(dto.getCapacity());
        table.setIsActive(dto.isActive());
        return mapToResponse(tableRepository.save(table));
    }

    @Override
    @Transactional
    public void deleteTable(Integer id) {
        CoffeeTable table = tableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bàn"));
        tableRepository.delete(table);
    }

    private CoffeeTableResponse mapToResponse(CoffeeTable table) {
        return CoffeeTableResponse.builder()
                .id(table.getTableId())
                .capacity(table.getCapacity())
                .isActive(table.getIsActive() != null ? table.getIsActive() : true)
                .build();
    }
}