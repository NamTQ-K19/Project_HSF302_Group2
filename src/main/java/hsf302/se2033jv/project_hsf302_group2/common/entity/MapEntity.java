package hsf302.se2033jv.project_hsf302_group2.common.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity ánh xạ tới bảng 'map'
 */
@Entity
@Table(name = "map")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MapEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "map_id")
    private Integer mapId;

    @Column(name = "map_name", length = 100, nullable = false)
    private String mapName;

    @Column(name = "url_map", length = 255, nullable = false)
    private String urlMap;
}

