package com.ducami.dukkaebi.domain.grading.service;

import com.ducami.dukkaebi.domain.contest.domain.repo.ContestJpaRepo;
import com.ducami.dukkaebi.domain.contest.domain.repo.ContestParticipantJpaRepo;
import com.ducami.dukkaebi.domain.contest.domain.repo.ContestProblemScoreJpaRepo;
import com.ducami.dukkaebi.domain.contest.domain.repo.ContestSubmissionJpaRepo;
import com.ducami.dukkaebi.domain.grading.domain.enums.JudgeStatus;
import com.ducami.dukkaebi.domain.grading.model.ExecutionResult;
import com.ducami.dukkaebi.domain.grading.presentation.dto.response.JudgeResultRes;
import com.ducami.dukkaebi.domain.grading.util.CodeExecutor;
import com.ducami.dukkaebi.domain.problem.domain.Problem;
import com.ducami.dukkaebi.domain.problem.domain.ProblemHistory;
import com.ducami.dukkaebi.domain.problem.domain.ProblemTestCase;
import com.ducami.dukkaebi.domain.problem.domain.enums.DifficultyType;
import com.ducami.dukkaebi.domain.problem.domain.enums.SolvedResult;
import com.ducami.dukkaebi.domain.problem.domain.repo.ProblemHistoryJpaRepo;
import com.ducami.dukkaebi.domain.problem.domain.repo.ProblemJpaRepo;
import com.ducami.dukkaebi.domain.problem.domain.repo.ProblemTestCaseJpaRepo;
import com.ducami.dukkaebi.domain.user.domain.User;
import com.ducami.dukkaebi.domain.user.domain.enums.GrowthType;
import com.ducami.dukkaebi.domain.user.domain.enums.UserType;
import com.ducami.dukkaebi.domain.user.domain.repo.UserJpaRepo;
import com.ducami.dukkaebi.domain.user.service.UserActivityService;
import com.ducami.dukkaebi.global.security.auth.UserSessionHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JudgeServiceTest {

    private static final Long PROBLEM_ID = 1L;
    private static final Long USER_ID = 1L;
    private static final String SOURCE_CODE = "print(42)";

    @Mock
    private ProblemJpaRepo problemJpaRepo;
    @Mock
    private ProblemTestCaseJpaRepo testCaseJpaRepo;
    @Mock
    private ProblemHistoryJpaRepo problemHistoryJpaRepo;
    @Mock
    private CodeExecutor codeExecutor;
    @Mock
    private UserSessionHolder userSessionHolder;
    @Mock
    private UserJpaRepo userJpaRepo;
    @Mock
    private UserActivityService userActivityService;
    @Mock
    private ContestParticipantJpaRepo contestParticipantJpaRepo;
    @Mock
    private ContestProblemScoreJpaRepo contestProblemScoreJpaRepo;
    @Mock
    private ContestSubmissionJpaRepo contestSubmissionJpaRepo;
    @Mock
    private ContestJpaRepo contestJpaRepo;

    @InjectMocks
    private JudgeService judgeService;

    private Problem problem;
    private User user;
    private AtomicReference<ProblemHistory> storedHistory;

    @BeforeEach
    void setUp() {
        problem = Problem.builder()
                .problemId(PROBLEM_ID)
                .name("테스트 문제")
                .description("테스트 설명")
                .input("입력")
                .output("출력")
                .difficulty(DifficultyType.COPPER)
                .solvedCount(0)
                .attemptCount(0)
                .addedAt(LocalDate.now())
                .build();

        user = User.builder()
                .id(USER_ID)
                .loginId("student")
                .password("password")
                .nickname("학생")
                .role(UserType.STUDENT)
                .score(0)
                .growth(GrowthType.WISP)
                .build();

        ProblemTestCase testCase = ProblemTestCase.builder()
                .id(1L)
                .problem(problem)
                .input("")
                .output("42")
                .build();

        storedHistory = new AtomicReference<>();

        when(problemJpaRepo.findById(PROBLEM_ID)).thenReturn(Optional.of(problem));
        when(testCaseJpaRepo.findByProblem_ProblemId(PROBLEM_ID)).thenReturn(List.of(testCase));
        when(userSessionHolder.getUser()).thenReturn(user);
        when(userJpaRepo.findById(USER_ID)).thenReturn(Optional.of(user));
        when(problemHistoryJpaRepo.findByUser_IdAndProblem_ProblemId(USER_ID, PROBLEM_ID))
                .thenAnswer(ignored -> Optional.ofNullable(storedHistory.get()));
        when(problemHistoryJpaRepo.save(any(ProblemHistory.class))).thenAnswer(invocation -> {
            ProblemHistory history = invocation.getArgument(0);
            storedHistory.set(history);
            return history;
        });
    }

    @Test
    @DisplayName("첫 제출이 정답이면 SOLVED로 저장하고 최초 보상을 반영한다")
    void firstAcceptedSubmissionStoresSolved() {
        gradeAsAccepted();

        JudgeResultRes result = judgeService.judgeCode(PROBLEM_ID, SOURCE_CODE, "python", null);

        assertThat(result.status()).isEqualTo(JudgeStatus.ACCEPTED);
        assertThat(storedHistory.get().getSolvedResult()).isEqualTo(SolvedResult.SOLVED);
        assertThat(problem.getSolvedCount()).isEqualTo(1);
        assertThat(problem.getAttemptCount()).isEqualTo(1);
        assertThat(user.getScore()).isEqualTo(1);
        verify(userActivityService).increaseTodaySolvedCount(1);
    }

    @Test
    @DisplayName("첫 제출이 오답이면 FAILED로 저장하고 최초 시도만 반영한다")
    void firstWrongSubmissionStoresFailed() {
        gradeAsWrongAnswer();

        JudgeResultRes result = judgeService.judgeCode(PROBLEM_ID, SOURCE_CODE, "python", null);

        assertThat(result.status()).isEqualTo(JudgeStatus.WRONG_ANSWER);
        assertThat(storedHistory.get().getSolvedResult()).isEqualTo(SolvedResult.FAILED);
        assertThat(problem.getSolvedCount()).isZero();
        assertThat(problem.getAttemptCount()).isEqualTo(1);
        assertThat(user.getScore()).isZero();
        verifyNoInteractions(userActivityService);
    }

    @Test
    @DisplayName("오답 후 정답이면 FAILED를 SOLVED로 변경하고 보상을 한 번만 반영한다")
    void acceptedAfterWrongStoresSolved() {
        when(codeExecutor.execute(anyString(), anyString(), anyString(), anyLong()))
                .thenReturn(wrongAnswer(), accepted());

        JudgeResultRes wrongResult = judgeService.judgeCode(PROBLEM_ID, SOURCE_CODE, "python", null);
        JudgeResultRes acceptedResult = judgeService.judgeCode(PROBLEM_ID, SOURCE_CODE, "python", null);

        assertThat(wrongResult.status()).isEqualTo(JudgeStatus.WRONG_ANSWER);
        assertThat(acceptedResult.status()).isEqualTo(JudgeStatus.ACCEPTED);
        assertThat(storedHistory.get().getSolvedResult()).isEqualTo(SolvedResult.SOLVED);
        assertThat(problem.getSolvedCount()).isEqualTo(1);
        assertThat(problem.getAttemptCount()).isEqualTo(1);
        assertThat(user.getScore()).isEqualTo(1);
        verify(userActivityService).increaseTodaySolvedCount(1);
        verify(problemHistoryJpaRepo, times(2)).save(any(ProblemHistory.class));
    }

    @Test
    @DisplayName("정답 후 오답을 제출해도 저장된 SOLVED 상태를 FAILED로 내리지 않는다")
    void wrongAfterAcceptedKeepsSolved() {
        when(codeExecutor.execute(anyString(), anyString(), anyString(), anyLong()))
                .thenReturn(accepted(), wrongAnswer());

        JudgeResultRes acceptedResult = judgeService.judgeCode(PROBLEM_ID, SOURCE_CODE, "python", null);
        JudgeResultRes wrongResult = judgeService.judgeCode(PROBLEM_ID, SOURCE_CODE, "python", null);

        assertThat(acceptedResult.status()).isEqualTo(JudgeStatus.ACCEPTED);
        assertThat(wrongResult.status()).isEqualTo(JudgeStatus.WRONG_ANSWER);
        assertThat(storedHistory.get().getSolvedResult()).isEqualTo(SolvedResult.SOLVED);
        assertThat(problem.getSolvedCount()).isEqualTo(1);
        assertThat(problem.getAttemptCount()).isEqualTo(1);
        assertThat(user.getScore()).isEqualTo(1);
        verify(userActivityService).increaseTodaySolvedCount(1);
        verify(problemHistoryJpaRepo).save(any(ProblemHistory.class));
    }

    @Test
    @DisplayName("정답 후 계속 정답을 제출해도 시도 사용자 수와 오늘 기여도는 증가하지 않는다")
    void repeatedAcceptedSubmissionsDoNotIncreaseCounts() {
        when(codeExecutor.execute(anyString(), anyString(), anyString(), anyLong()))
                .thenReturn(accepted(), accepted(), accepted());

        judgeService.judgeCode(PROBLEM_ID, SOURCE_CODE, "python", null);
        judgeService.judgeCode(PROBLEM_ID, SOURCE_CODE, "python", null);
        judgeService.judgeCode(PROBLEM_ID, SOURCE_CODE, "python", null);

        assertThat(storedHistory.get().getSolvedResult()).isEqualTo(SolvedResult.SOLVED);
        assertThat(problem.getSolvedCount()).isEqualTo(1);
        assertThat(problem.getAttemptCount()).isEqualTo(1);
        assertThat(user.getScore()).isEqualTo(1);
        verify(userActivityService).increaseTodaySolvedCount(1);
        verify(problemJpaRepo).save(problem);
        verify(problemHistoryJpaRepo).save(any(ProblemHistory.class));
    }

    private void gradeAsAccepted() {
        when(codeExecutor.execute(anyString(), anyString(), anyString(), anyLong()))
                .thenReturn(accepted());
    }

    private void gradeAsWrongAnswer() {
        when(codeExecutor.execute(anyString(), anyString(), anyString(), anyLong()))
                .thenReturn(wrongAnswer());
    }

    private ExecutionResult accepted() {
        return new ExecutionResult("42", "", true, false);
    }

    private ExecutionResult wrongAnswer() {
        return new ExecutionResult("0", "", true, false);
    }
}
