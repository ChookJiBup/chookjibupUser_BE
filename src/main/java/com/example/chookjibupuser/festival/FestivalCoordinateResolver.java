package com.example.chookjibupuser.festival;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 축제의 위경도를 정한다.
 *
 * <p>같은 데이터베이스인데 좌표를 두는 자리가 둘이다. 관리자 콘솔에서 등록한 축제는
 * 좌표를 `festival_locations`의 대표 위치 행에 두고(그쪽 SSOT), `festivals` 테이블의
 * latitude/longitude 컬럼은 문화체육관광부 임포트 파이프라인만 채운다. 방문객 앱은
 * 그동안 후자만 읽어서, <strong>관리자가 만든 축제는 주소는 멀쩡한데 좌표가 비어
 * 전체 축제 지도에서 조용히 빠졌다.</strong></p>
 *
 * <p>그래서 대표 위치를 먼저 보고, 없으면 축제 행의 값으로 떨어진다. 한쪽으로
 * 갈아치우지 않는 이유는 임포트 축제에는 `festival_locations` 행이 아예 없을 수 있어서다.</p>
 */
@Component
@RequiredArgsConstructor
public class FestivalCoordinateResolver {

    private final FestivalRepository festivalRepository;

    public Map<Long, FestivalCoordinate> resolve(Collection<Festival> festivals) {
        if (festivals.isEmpty()) {
            return Map.of();
        }
        List<Long> ids = festivals.stream().map(Festival::getFestivalId).toList();
        return merge(festivals, festivalRepository.findPrimaryCoordinates(ids));
    }

    public FestivalCoordinate resolve(Festival festival) {
        return resolve(List.of(festival))
                .getOrDefault(festival.getFestivalId(), FestivalCoordinate.NONE);
    }

    /**
     * 축제별 좌표를 고른다.
     *
     * <p>대표 위치가 여러 건인 축제가 생기더라도 결과가 호출할 때마다 달라지지 않도록,
     * location_id가 가장 작은(= 가장 먼저 만든) 행 하나만 쓴다.</p>
     */
    static Map<Long, FestivalCoordinate> merge(
            Collection<Festival> festivals,
            List<FestivalPrimaryCoordinateRow> primaryRows
    ) {
        Map<Long, FestivalPrimaryCoordinateRow> chosen = new HashMap<>();
        primaryRows.forEach(row -> chosen.merge(
                row.getFestivalId(),
                row,
                (kept, candidate) -> candidate.getLocationId() < kept.getLocationId() ? candidate : kept
        ));

        Map<Long, FestivalCoordinate> resolved = new HashMap<>();
        festivals.forEach(festival -> {
            FestivalPrimaryCoordinateRow primary = chosen.get(festival.getFestivalId());
            resolved.put(
                    festival.getFestivalId(),
                    primary != null
                            ? new FestivalCoordinate(primary.getLatitude(), primary.getLongitude())
                            : new FestivalCoordinate(festival.getLatitude(), festival.getLongitude())
            );
        });
        return resolved;
    }
}
