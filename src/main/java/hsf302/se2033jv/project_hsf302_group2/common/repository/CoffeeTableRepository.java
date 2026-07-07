package hsf302.se2033jv.project_hsf302_group2.common.repository;

import hsf302.se2033jv.project_hsf302_group2.common.entity.CoffeeTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CoffeeTableRepository extends JpaRepository<CoffeeTable, Integer> {
    // Các hàm CRUD cơ bản (findAll, save, delete, findById) đã được JpaRepository lo hết rồi!
}
