package hsf302.se2033jv.project_hsf302_group2.manager.service.interfaces;

import hsf302.se2033jv.project_hsf302_group2.manager.dto.request.CoffeeTableRequest;
import hsf302.se2033jv.project_hsf302_group2.manager.dto.response.CoffeeTableResponse;
import java.util.List;

public interface ICoffeeTableService {
    List<CoffeeTableResponse> getAllTables();
    CoffeeTableResponse createTable(CoffeeTableRequest dto);
    CoffeeTableResponse updateTable(Integer id, CoffeeTableRequest dto);
    void toggleTableStatus(Integer id);
}
