package com.ducami.dukkaebi.domain.contest.usecase;

import com.ducami.dukkaebi.domain.contest.domain.Contest;
import com.ducami.dukkaebi.domain.contest.domain.ContestParticipant;
import com.ducami.dukkaebi.domain.contest.domain.enums.ContestStatus;
import com.ducami.dukkaebi.domain.contest.domain.repo.ContestJpaRepo;
import com.ducami.dukkaebi.domain.contest.domain.repo.ContestParticipantJpaRepo;
import com.ducami.dukkaebi.domain.contest.domain.repo.ContestProblemMappingJpaRepo;
import com.ducami.dukkaebi.domain.contest.domain.repo.ContestProblemScoreJpaRepo;
import com.ducami.dukkaebi.domain.contest.domain.repo.ContestSubmissionJpaRepo;
import com.ducami.dukkaebi.domain.contest.presentation.dto.response.ContestListRes;
import com.ducami.dukkaebi.domain.contest.service.ContestSseService;
import com.ducami.dukkaebi.domain.contest.util.CodeGenerator;
import com.ducami.dukkaebi.domain.grading.domain.repo.SavedCodeJpaRepo;
import com.ducami.dukkaebi.domain.problem.domain.repo.ProblemHistoryJpaRepo;
import com.ducami.dukkaebi.domain.problem.domain.repo.ProblemJpaRepo;
import com.ducami.dukkaebi.domain.problem.domain.repo.ProblemTestCaseJpaRepo;
import com.ducami.dukkaebi.global.common.dto.response.PageResponse;
import com.ducami.dukkaebi.global.security.auth.UserSessionHolder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContestUseCaseTest {

    private static final long USER_ID = 42L;
    private static final int PAGE = 0;
    private static final int SIZE = 12;
    private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");
    private static final LocalDateTime FIXED_NOW = LocalDateTime.of(2026, 7, 22, 12, 0);

    @Mock
    private ContestJpaRepo contestJpaRepo;
    @Mock
    private ContestParticipantJpaRepo contestParticipantJpaRepo;
    @Mock
    private ContestProblemScoreJpaRepo contestProblemScoreJpaRepo;
    @Mock
    private ContestSubmissionJpaRepo contestSubmissionJpaRepo;
    @Mock
    private ContestProblemMappingJpaRepo contestProblemMappingJpaRepo;
    @Mock
    private CodeGenerator codeGenerator;
    @Mock
    private UserSessionHolder userSessionHolder;
    @Mock
    private ProblemJpaRepo problemJpaRepo;
    @Mock
    private ProblemHistoryJpaRepo problemHistoryJpaRepo;
    @Mock
    private ProblemTestCaseJpaRepo problemTestCaseJpaRepo;
    @Mock
    private SavedCodeJpaRepo savedCodeJpaRepo;
    @Mock
    private ContestSseService contestSseService;

    @InjectMocks
    private ContestUseCase contestUseCase;

    @ParameterizedTest(name = "{0}")
    @MethodSource("statusBoundaryCases")
    @DisplayName("대회 목록 상태는 시작일과 종료일 경계값에 따라 결정된다")
    void determinesStatusAtDateBoundaries(
            String scenario,
            LocalDateTime startDate,
            LocalDateTime endDate,
            ContestStatus expectedStatus
    ) {
        Contest contest = contest("contest", startDate, endDate, ContestStatus.UPCOMING);
        stubContestPage(List.of(contest));
        when(contestParticipantJpaRepo.findAllByContest_CodeInAndUser_Id(List.of("contest"), USER_ID))
                .thenReturn(List.of());

        ContestListRes result = getContestListAtFixedNow().getContent().getFirst();

        assertThat(result.status()).as(scenario).isEqualTo(expectedStatus);
    }

    @Test
    @DisplayName("관리자가 종료한 대회는 날짜와 관계없이 ENDED 상태로 조회된다")
    void forcedEndedStatusTakesPrecedenceOverDates() {
        Contest contest = contest(
                "forced-ended",
                FIXED_NOW.plusDays(1),
                FIXED_NOW.plusDays(2),
                ContestStatus.ENDED
        );
        stubContestPage(List.of(contest));
        when(contestParticipantJpaRepo.findAllByContest_CodeInAndUser_Id(List.of("forced-ended"), USER_ID))
                .thenReturn(List.of());

        ContestListRes result = getContestListAtFixedNow().getContent().getFirst();

        assertThat(result.status()).isEqualTo(ContestStatus.ENDED);
    }

    @Test
    @DisplayName("참가 기록이 있는 대회만 joined가 true로 조회된다")
    void mapsJoinedStatusByContestParticipation() {
        Contest notJoinedContest = ongoingContest("not-joined");
        Contest joinedContest = ongoingContest("joined");
        ContestParticipant participant = ContestParticipant.builder()
                .id(1L)
                .contest(joinedContest)
                .totalScore(0)
                .totalTimeSeconds(0)
                .joinedAt(FIXED_NOW.minusHours(1))
                .build();

        stubContestPage(List.of(notJoinedContest, joinedContest));
        when(contestParticipantJpaRepo.findAllByContest_CodeInAndUser_Id(
                List.of("not-joined", "joined"), USER_ID
        )).thenReturn(List.of(participant));

        PageResponse<ContestListRes> result = getContestListAtFixedNow();

        assertThat(result.getContent())
                .extracting(ContestListRes::code, ContestListRes::joined)
                .containsExactly(
                        tuple("not-joined", false),
                        tuple("joined", true)
                );
        assertThat(result.getCurrentPage()).isEqualTo(PAGE);
        assertThat(result.getSize()).isEqualTo(SIZE);
        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    @DisplayName("대회 목록이 비어 있으면 빈 페이지를 반환하고 참가 기록을 조회하지 않는다")
    void returnsEmptyPageWithoutLookingUpParticipants() {
        stubContestPage(List.of());

        PageResponse<ContestListRes> result = getContestListAtFixedNow();

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
        verifyNoInteractions(contestParticipantJpaRepo);
    }

    private static Stream<Arguments> statusBoundaryCases() {
        return Stream.of(
                Arguments.of(
                        "시작 시각을 1ns 지난 대회는 ONGOING이다",
                        FIXED_NOW.minusNanos(1),
                        FIXED_NOW.plusDays(1),
                        ContestStatus.ONGOING
                ),
                Arguments.of(
                        "시작 시각과 현재 시각이 같으면 ONGOING이다",
                        FIXED_NOW,
                        FIXED_NOW.plusDays(1),
                        ContestStatus.ONGOING
                ),
                Arguments.of(
                        "시작 시각이 1ns 남은 대회는 UPCOMING이다",
                        FIXED_NOW.plusNanos(1),
                        FIXED_NOW.plusDays(1),
                        ContestStatus.UPCOMING
                ),
                Arguments.of(
                        "종료 시각을 1ns 지난 대회는 ENDED이다",
                        FIXED_NOW.minusDays(1),
                        FIXED_NOW.minusNanos(1),
                        ContestStatus.ENDED
                ),
                Arguments.of(
                        "종료 시각과 현재 시각이 같으면 ENDED이다",
                        FIXED_NOW.minusDays(1),
                        FIXED_NOW,
                        ContestStatus.ENDED
                ),
                Arguments.of(
                        "종료 시각이 1ns 남은 대회는 ONGOING이다",
                        FIXED_NOW.minusDays(1),
                        FIXED_NOW.plusNanos(1),
                        ContestStatus.ONGOING
                )
        );
    }

    private Contest ongoingContest(String code) {
        return contest(
                code,
                FIXED_NOW.minusDays(1),
                FIXED_NOW.plusDays(1),
                ContestStatus.UPCOMING
        );
    }

    private Contest contest(
            String code,
            LocalDateTime startDate,
            LocalDateTime endDate,
            ContestStatus status
    ) {
        return Contest.builder()
                .code(code)
                .title(code + " 대회")
                .description("테스트 대회")
                .startDate(startDate)
                .endDate(endDate)
                .status(status)
                .participantIds(List.of())
                .problemIds(List.of())
                .build();
    }

    private void stubContestPage(List<Contest> contests) {
        PageRequest pageRequest = PageRequest.of(PAGE, SIZE);
        when(userSessionHolder.getUserId()).thenReturn(USER_ID);
        when(contestJpaRepo.findAllByOrderByEndDateAsc(pageRequest))
                .thenReturn(new PageImpl<>(contests, pageRequest, contests.size()));
    }

    private PageResponse<ContestListRes> getContestListAtFixedNow() {
        try (MockedStatic<LocalDateTime> localDateTime = mockStatic(LocalDateTime.class, CALLS_REAL_METHODS)) {
            localDateTime.when(() -> LocalDateTime.now(SEOUL_ZONE)).thenReturn(FIXED_NOW);
            return contestUseCase.getContestListPaged(PAGE, SIZE);
        }
    }
}
