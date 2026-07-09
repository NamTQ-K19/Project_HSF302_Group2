package hsf302.se2033jv.project_hsf302_group2.common.repository;

import hsf302.se2033jv.project_hsf302_group2.common.entity.CoffeeTable;
import hsf302.se2033jv.project_hsf302_group2.common.enums.TableStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CoffeeTableRepository extends JpaRepository<CoffeeTable, Integer> {
    List<CoffeeTable> findByIsActiveTrueOrderByTableIdAsc();
    List<CoffeeTable> findByIsActiveTrueAndStatusOrderByTableIdAsc(TableStatus status);
}
