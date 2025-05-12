# Batch Client

>

<br>

## Batch 시나리오

1. Chunk VS Tasklet
    - RoomType 기준 자동 예약 오픈 설정 정책: 20,000 rows
    - 이 중 절반은 이미 예약 오픈 되어 있는 상태를 만든다. (180일치 180만 rows)

- Chunk 방식
    - chunk size: 6
    - read size: 100
    - processor 결과에 따라 write 개수 랜덤
    - writer 개수 범위: 0 ~ 6 * 100 * 180 = 108,000

- Tasklet 방식
    - read size: 100
    - processor 결과에 따라 read 반복
    - base line writer 개수: 100,000

나머지 180만 rows를 예약 오픈 상태로 변경하는 작업을 한다.
작업 시간을 측정한다.

Step: [openRoomAvailabilityChunkStep] executed in 6m29s526ms
Step: [openRoomAvailabilityChunkStep] executed in 2m2s749ms
Step: [openRoomAvailabilityChunkStep] executed in 2m29s618ms

Job: [SimpleJob: [name=openRoomAvailabilityChunkJob]] completed with the following
parameters: [{'run.id':'{value=1746407416198, type=class java.lang.Long, identifying=true}'}]
and the following status: [COMPLETED] in 6m29s557ms
Job: [SimpleJob: [name=openRoomAvailabilityChunkJob]] completed with the following
parameters: [{'run.id':'{value=1746411150017, type=class java.lang.Long, identifying=true}'}]
and the following status: [COMPLETED] in 2m2s783ms

Commit/READ/FILTER/WRITE
34 / 200 / 0 / 200
Commit/READ/FILTER/WRITE
34 / 200 / 0 / 200

Step: [openRoomAvailabilityTaskletStep] executed in 6m29s539ms
Step: [openRoomAvailabilityTaskletStep] executed in 2m2s999ms
Step: [openRoomAvailabilityTaskletStep] executed in 2m27s994ms

Job: [SimpleJob: [name=openRoomAvailabilityTaskletJob]] completed with the following
parameters: [{'run.id':'{value=1746408098685, type=class java.lang.Long, identifying=true}'}]
and the following status: [COMPLETED] in 6m29s566ms
Job: [SimpleJob: [name=openRoomAvailabilityTaskletJob]] completed with the following
parameters: [{'run.id':'{value=1746411388736, type=class java.lang.Long, identifying=true}'}]
and the following status: [COMPLETED] in 2m3s30ms

Commit/READ/FILTER/WRITE
17 / 200 / 200 / 17
---

1. processor 개선 parallel stream

// // 날짜별로 RoomAvailability 생성
// for (LocalDate startDate = today; startDate.isBefore(endDay); startDate = startDate.plusDays(1)) {
// List<RoomAvailability> createRoomAvailabilities =
// createRoomAvailabilitiesMatchDateAndExisted(
// startDate, roomTypes, findAvailabilityInRoomIdsResults, roomPricingPolicies);
//
// result.addAll(createRoomAvailabilities);
// }
// log.info("finished processing RoomAvailability size {}", result.size());
//
// return result;
