package com.ducami.dukkaebi.domain.contest.usecase;

import com.ducami.dukkaebi.domain.contest.domain.Contest;
import com.ducami.dukkaebi.domain.contest.domain.ContestParticipant;
import com.ducami.dukkaebi.domain.contest.domain.ContestProblemMapping;
import com.ducami.dukkaebi.domain.contest.domain.ContestProblemScore;
import com.ducami.dukkaebi.domain.contest.domain.ContestSubmission;
import com.ducami.dukkaebi.domain.contest.domain.repo.ContestJpaRepo;
import com.ducami.dukkaebi.domain.contest.domain.repo.ContestParticipantJpaRepo;
import com.ducami.dukkaebi.domain.contest.domain.repo.ContestProblemMappingJpaRepo;
import com.ducami.dukkaebi.domain.contest.domain.repo.ContestProblemScoreJpaRepo;
import com.ducami.dukkaebi.domain.contest.domain.repo.ContestSubmissionJpaRepo;
import com.ducami.dukkaebi.domain.contest.error.ContestErrorCode;
import com.ducami.dukkaebi.domain.contest.presentation.dto.request.ContestReq;
import com.ducami.dukkaebi.domain.contest.presentation.dto.request.ContestScoreUpdateReq;
import com.ducami.dukkaebi.domain.contest.presentation.dto.response.ContestDetailRes;
import com.ducami.dukkaebi.domain.contest.presentation.dto.response.ContestListRes;
import com.ducami.dukkaebi.domain.contest.presentation.dto.response.ContestParticipantListRes;
import com.ducami.dukkaebi.domain.contest.presentation.dto.response.ContestSubmissionRes;
import com.ducami.dukkaebi.domain.contest.presentation.dto.response.ContestUpdateEvent;
import com.ducami.dukkaebi.domain.contest.service.ContestSseService;
import com.ducami.dukkaebi.domain.contest.util.CodeGenerator;
import com.ducami.dukkaebi.domain.grading.domain.repo.SavedCodeJpaRepo;
import com.ducami.dukkaebi.domain.problem.domain.enums.DifficultyType;
import com.ducami.dukkaebi.domain.problem.error.ProblemErrorCode;
import com.ducami.dukkaebi.domain.problem.presentation.dto.request.ProblemUpdateReq;
import com.ducami.dukkaebi.domain.user.domain.User;
import com.ducami.dukkaebi.global.common.dto.response.PageResponse;
import com.ducami.dukkaebi.global.common.dto.response.Response;
import com.ducami.dukkaebi.global.exception.CustomException;
import com.ducami.dukkaebi.global.security.auth.UserSessionHolder;
import com.ducami.dukkaebi.domain.problem.domain.Problem;
import com.ducami.dukkaebi.domain.problem.domain.ProblemHistory;
import com.ducami.dukkaebi.domain.problem.domain.ProblemTestCase;
import com.ducami.dukkaebi.domain.problem.domain.repo.ProblemHistoryJpaRepo;
import com.ducami.dukkaebi.domain.problem.domain.repo.ProblemJpaRepo;
import com.ducami.dukkaebi.domain.problem.domain.repo.ProblemTestCaseJpaRepo;
import com.ducami.dukkaebi.domain.problem.presentation.dto.request.ProblemCreateReq;
import com.ducami.dukkaebi.domain.problem.presentation.dto.response.ProblemRes;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ContestUseCase {
    private final ContestJpaRepo contestJpaRepo;
    private final ContestParticipantJpaRepo contestParticipantJpaRepo;
    private final ContestProblemScoreJpaRepo contestProblemScoreJpaRepo;
    private final ContestSubmissionJpaRepo contestSubmissionJpaRepo;
    private final ContestProblemMappingJpaRepo contestProblemMappingJpaRepo;
    private final CodeGenerator codeGenerator;
    private final UserSessionHolder userSessionHolder;
    private final ProblemJpaRepo problemJpaRepo;
    private final ProblemHistoryJpaRepo problemHistoryJpaRepo;
    private final ProblemTestCaseJpaRepo problemTestCaseJpaRepo;
    private final SavedCodeJpaRepo savedCodeJpaRepo;
    private final ContestSseService contestSseService;

    private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");

    @Transactional(readOnly = true)
    public PageResponse<ContestListRes> getContestListPaged(int page, int size) {
        Long userId = userSessionHolder.getUserId();

        Pageable pageable = PageRequest.of(page, size);
        Page<ContestListRes> contestPage = contestJpaRepo.findAllByOrderByEndDateAsc(pageable)
                .map(contest -> ContestListRes.from(contest, userId));
        return PageResponse.of(contestPage);
    }

    @Transactional(readOnly = true)
    public ContestDetailRes getContestDetail(String code) {
        Contest contest = contestJpaRepo.findById(code)
                .orElseThrow(() -> new CustomException(ContestErrorCode.CONTEST_NOT_FOUND));

        Long userId = null;
        try { userId = userSessionHolder.getUserId(); } catch (Exception ignored) {}

        List<Long> problemIds = contest.getProblemIds() != null ? contest.getProblemIds() : List.of();
        List<Problem> problems = problemIds.isEmpty() ? List.of() : problemJpaRepo.findAllById(problemIds);

        List<ProblemRes> problemResList = new ArrayList<>();
        for (Problem p : problems) {
            ProblemHistory history = null;
            if (userId != null) {
                history = problemHistoryJpaRepo.findByUser_IdAndProblem_ProblemId(userId, p.getProblemId()).orElse(null);
            }

            // 일반 문제의 경우 ContestProblemMapping에서 점수 조회
            Integer contestScore = null;
            if (p.getContestId() == null) {
                // 일반 문제인 경우 매핑 테이블에서 점수 조회
                contestScore = contestProblemMappingJpaRepo.findByContest_CodeAndProblem_ProblemId(code, p.getProblemId())
                        .map(ContestProblemMapping::getScore)
                        .orElse(null);
            }

            problemResList.add(ProblemRes.from(p, history, contestScore));
        }

        return ContestDetailRes.from(contest, problemResList, userId);
    }

    @Transactional(readOnly = true)
    public PageResponse<ContestListRes> getContestWithNamePaged(String name, int page, int size) {
        Long userId = userSessionHolder.getUserId();

        Pageable pageable = PageRequest.of(page, size);
        Page<ContestListRes> contestPage = contestJpaRepo.findByTitleContainingIgnoreCaseOrderByEndDateAsc(name, pageable)
                .map(contest -> ContestListRes.from(contest, userId));
        return PageResponse.of(contestPage);
    }

    // 관리자
    @Transactional
    public Response createContest(ContestReq req) {
        if (contestJpaRepo.existsByTitle(req.title())) {
            throw new CustomException(ContestErrorCode.TITLE_ALREADY);
        }

        String code = codeGenerator.generateCode();
        while (contestJpaRepo.existsById(code)) {
            code = codeGenerator.generateCode();
        }

        contestJpaRepo.save(ContestReq.fromReq(code, req));
        return Response.created("대회가 성공적으로 생성되었습니다.");
    }

    @Transactional
    public Response updateContest(String code, ContestReq req) {
        Contest contest = contestJpaRepo.findById(code)
                .orElseThrow(() -> new CustomException(ContestErrorCode.CONTEST_NOT_FOUND));

        if (!contest.getTitle().equals(req.title()) && contestJpaRepo.existsByTitle(req.title())) {
            throw new CustomException(ContestErrorCode.TITLE_ALREADY);
        }

        contest.updateContest(req.title(), req.description(), req.imageUrl(), req.startDate(), req.endDate());
        contestJpaRepo.save(contest);

        // SSE 이벤트 발행: 대회를 구독 중인 모든 사용자에게 변경사항 전송
        ContestUpdateEvent event = ContestUpdateEvent.of(
                code,
                req.title(),
                req.description(),
                req.startDate(),
                req.endDate()
        );
        contestSseService.sendUpdateEvent(code, event);

        return Response.ok("대회가 성공적으로 수정되었습니다.");
    }

    @Transactional
    public Response deleteContest(String code) {
        Contest contest = contestJpaRepo.findById(code)
                .orElseThrow(() -> new CustomException(ContestErrorCode.CONTEST_NOT_FOUND));

        // 1. 대회 전용 문제들 조회 (contestId == code)
        List<Problem> contestOnlyProblems = problemJpaRepo.findAll().stream()
                .filter(p -> code.equals(p.getContestId()))
                .toList();

        // 2. 대회에 추가된 모든 문제 ID 조회 (대회 전용 + 일반 문제)
        List<Long> allProblemIds = contest.getProblemIds() != null ? contest.getProblemIds() : List.of();

        // 3. 대회 참가자들 조회
        List<ContestParticipant> participants = contestParticipantJpaRepo
                .findByContest_CodeOrderByTotalScoreDescTotalTimeSecondsAsc(code);

        // 4. 모든 문제에 대한 ContestSubmission 삭제 (제출 코드)
        for (Long problemId : allProblemIds) {
            for (ContestParticipant participant : participants) {
                contestSubmissionJpaRepo.findFirstByContest_CodeAndUser_IdAndProblem_ProblemIdOrderBySubmittedAtDesc(
                        code, participant.getUser().getId(), problemId
                ).ifPresent(contestSubmissionJpaRepo::delete);
            }
        }

        // 5. ContestProblemScore 삭제 (모든 문제별 점수)
        for (ContestParticipant participant : participants) {
            List<ContestProblemScore> scores = contestProblemScoreJpaRepo.findByParticipant_Id(participant.getId());
            contestProblemScoreJpaRepo.deleteAll(scores);
        }

        // 6. ContestParticipant 삭제 (참가자)
        contestParticipantJpaRepo.deleteAll(participants);

        // 7. 대회 전용 문제의 테스트케이스와 ProblemHistory 삭제
        for (Problem problem : contestOnlyProblems) {
            // 테스트케이스 삭제
            List<ProblemTestCase> testCases = problemTestCaseJpaRepo.findByProblem_ProblemId(problem.getProblemId());
            problemTestCaseJpaRepo.deleteAll(testCases);

            // ProblemHistory 삭제
            problemHistoryJpaRepo.deleteAll(
                    problemHistoryJpaRepo.findAll().stream()
                            .filter(h -> h.getProblem().getProblemId().equals(problem.getProblemId()))
                            .toList()
            );

            // SavedCode 삭제 (대회 전용 문제에 저장된 코드)
            savedCodeJpaRepo.deleteByProblem_ProblemId(problem.getProblemId());
        }

        // 8. 대회 전용 문제들만 삭제 (일반 문제는 삭제하지 않음)
        problemJpaRepo.deleteAll(contestOnlyProblems);

        // 8. ContestProblemMapping 삭제 (일반 문제를 대회에 추가한 경우의 매핑)
        List<ContestProblemMapping> mappings = contestProblemMappingJpaRepo.findByContest_Code(code);
        contestProblemMappingJpaRepo.deleteAll(mappings);

        // 9. Contest의 participantIds와 problemIds 비우기
        if (contest.getParticipantIds() != null && !contest.getParticipantIds().isEmpty()) {
            contest.getParticipantIds().clear();
        }
        if (contest.getProblemIds() != null && !contest.getProblemIds().isEmpty()) {
            contest.getProblemIds().clear();
        }
        contestJpaRepo.saveAndFlush(contest);

        // 10. 대회 삭제
        contestJpaRepo.delete(contest);

        return Response.ok("대회가 성공적으로 삭제되었습니다.");
    }

    // 학생
    @Transactional
    public Response joinContest(String code) {
        Contest contest = contestJpaRepo.findById(code)
                .orElseThrow(() -> new CustomException(ContestErrorCode.CONTEST_NOT_FOUND));

        LocalDateTime now = LocalDateTime.now(ZONE);
        if (contest.getEndDate() != null && contest.getEndDate().isBefore(now)) {
            return Response.of(HttpStatus.BAD_REQUEST, "대회가 종료되었습니다.");
        }

        Long userId = userSessionHolder.getUserId();
        User user = userSessionHolder.getUser();

        if (contest.getParticipantIds() != null && contest.getParticipantIds().contains(userId)) {
            return Response.ok("이미 참여중입니다.");
        }

        // Contest에 참여자 추가
        contest.addParticipant(userId);
        contestJpaRepo.save(contest);

        // ContestParticipant 엔티티 생성 (점수/시간 추적용)
        ContestParticipant participant = ContestParticipant.builder()
                .contest(contest)
                .user(user)
                .totalScore(0)
                .totalTimeSeconds(0)
                .joinedAt(now)
                .build();
        contestParticipantJpaRepo.save(participant);

        return Response.ok("대회에 참가하였습니다.");
    }

    // 대회 전용 문제 생성
    @Transactional
    public Response createContestProblem(String code, ProblemCreateReq req) {
        Contest contest = contestJpaRepo.findById(code)
                .orElseThrow(() -> new CustomException(ContestErrorCode.CONTEST_NOT_FOUND));

        // 대회 전용 문제 생성
        Problem problem = req.toContestEntity(code);
        Problem savedProblem = problemJpaRepo.save(problem);

        // 테스트 케이스 저장
        if (req.testCases() != null && !req.testCases().isEmpty()) {
            for (ProblemCreateReq.TestCaseReq tcReq : req.testCases()) {
                ProblemTestCase testCase = ProblemTestCase.builder()
                        .problem(savedProblem)
                        .input(tcReq.input())
                        .output(tcReq.output())
                        .build();
                problemTestCaseJpaRepo.save(testCase);
            }
        }

        // 대회의 문제 목록에 추가
        List<Long> problemIds = contest.getProblemIds();
        if (problemIds == null) {
            problemIds = new ArrayList<>();
        }
        problemIds.add(savedProblem.getProblemId());
        contestJpaRepo.save(contest);

        return Response.created("대회 전용 문제가 성공적으로 생성되었습니다.");
    }

    // 일반 문제들을 대회에 추가
    @Transactional
    public Response addExistingProblemsToContest(String code, List<Long> problemIds) {
        Contest contest = contestJpaRepo.findById(code)
                .orElseThrow(() -> new CustomException(ContestErrorCode.CONTEST_NOT_FOUND));

        int addedCount = 0;
        StringBuilder resultMessage = new StringBuilder();

        for (Long problemId : problemIds) {
            try {
                Problem problem = problemJpaRepo.findById(problemId)
                        .orElseThrow(() -> new CustomException(ProblemErrorCode.PROBLEM_NOT_FOUND));

                // 일반 문제인지 확인 (contestId가 null이어야 함)
                if (problem.getContestId() != null) {
                    resultMessage.append("문제 ID ").append(problemId).append(": 대회 전용 문제는 추가할 수 없습니다.\n");
                    continue;
                }

                // 이미 추가된 문제인지 확인
                if (contestProblemMappingJpaRepo.findByContest_CodeAndProblem_ProblemId(code, problemId).isPresent()) {
                    resultMessage.append("문제 ID ").append(problemId).append(": 이미 대회에 추가된 문제입니다.\n");
                    continue;
                }

                // 난이도를 점수로 변환
                Integer score = difficultyToScore(problem.getDifficulty());

                // ContestProblemMapping 생성
                ContestProblemMapping mapping = ContestProblemMapping.builder()
                        .contest(contest)
                        .problem(problem)
                        .score(score)
                        .build();
                contestProblemMappingJpaRepo.save(mapping);

                // Contest의 problemIds에 추가
                List<Long> contestProblemIds = contest.getProblemIds();
                if (contestProblemIds == null) {
                    contestProblemIds = new ArrayList<>();
                }
                if (!contestProblemIds.contains(problemId)) {
                    contestProblemIds.add(problemId);
                }

                addedCount++;
                resultMessage.append("문제 ID ").append(problemId).append(": 성공 (점수: ").append(score).append("점)\n");

            } catch (Exception e) {
                resultMessage.append("문제 ID ").append(problemId).append(": 오류 - ").append(e.getMessage()).append("\n");
            }
        }

        contestJpaRepo.save(contest);

        return Response.created(addedCount + "개의 문제가 대회에 추가되었습니다.\n" + resultMessage.toString());
    }

    // 대회 문제 점수만 수정 (가져온 일반 문제용)
    @Transactional
    public Response updateContestProblemScore(String code, Long problemId, Integer score) {
        Contest contest = contestJpaRepo.findById(code)
                .orElseThrow(() -> new CustomException(ContestErrorCode.CONTEST_NOT_FOUND));

        // ContestProblemMapping 조회
        ContestProblemMapping mapping = contestProblemMappingJpaRepo
                .findByContest_CodeAndProblem_ProblemId(code, problemId)
                .orElseThrow(() -> new CustomException(ContestErrorCode.CONTEST_NOT_FOUND));

        mapping.updateScore(score);
        contestProblemMappingJpaRepo.save(mapping);

        return Response.ok("문제 점수가 성공적으로 수정되었습니다. (새 점수: " + score + "점)");
    }

    // 대회 문제 수정
    @Transactional
    public Response updateContestProblem(String code, Long problemId, ProblemUpdateReq req) {
        Contest contest = contestJpaRepo.findById(code)
                .orElseThrow(() -> new CustomException(ContestErrorCode.CONTEST_NOT_FOUND));

        Problem problem = problemJpaRepo.findById(problemId)
                .orElseThrow(() -> new CustomException(ProblemErrorCode.PROBLEM_NOT_FOUND));

        // 대회 전용 문제인지 확인
        if (!code.equals(problem.getContestId())) {
            throw new CustomException(ContestErrorCode.NOT_CONTEST_PROBLEM);
        }

        // 문제 정보 수정 (대회 문제는 difficulty는 null, score는 필수)
        problem.updateContestProblem(req.name(), req.description(), req.input(), req.output(), req.score());
        problemJpaRepo.save(problem);

        // 기존 테스트 케이스 삭제
        List<ProblemTestCase> existingTestCases = problemTestCaseJpaRepo.findByProblem_ProblemId(problemId);
        problemTestCaseJpaRepo.deleteAll(existingTestCases);

        // 새로운 테스트 케이스 저장
        if (req.testCases() != null && !req.testCases().isEmpty()) {
            for (ProblemCreateReq.TestCaseReq tcReq : req.testCases()) {
                ProblemTestCase testCase = ProblemTestCase.builder()
                        .problem(problem)
                        .input(tcReq.input())
                        .output(tcReq.output())
                        .build();
                problemTestCaseJpaRepo.save(testCase);
            }
        }

        return Response.ok("대회 문제가 성공적으로 수정되었습니다.");
    }

    // 대회 문제 삭제
    @Transactional
    public Response deleteContestProblem(String code, Long problemId) {
        Contest contest = contestJpaRepo.findById(code)
                .orElseThrow(() -> new CustomException(ContestErrorCode.CONTEST_NOT_FOUND));

        Problem problem = problemJpaRepo.findById(problemId)
                .orElseThrow(() -> new CustomException(ProblemErrorCode.PROBLEM_NOT_FOUND));

        // 대회 전용 문제인지 확인
        boolean isContestOnlyProblem = code.equals(problem.getContestId());

        if (isContestOnlyProblem) {
            // ===== 대회 전용 문제 (contestId != null) =====
            // 문제 자체를 삭제

            // 1. ContestProblemScore 삭제 (문제별 점수)
            List<ContestParticipant> participants = contestParticipantJpaRepo
                    .findByContest_CodeOrderByTotalScoreDescTotalTimeSecondsAsc(code);

            for (ContestParticipant participant : participants) {
                contestProblemScoreJpaRepo.findByParticipant_IdAndProblem_ProblemId(
                        participant.getId(), problemId
                ).ifPresent(contestProblemScoreJpaRepo::delete);
            }

            // 2. ProblemHistory 삭제 (풀이 기록)
            problemHistoryJpaRepo.deleteAll(
                    problemHistoryJpaRepo.findAll().stream()
                            .filter(h -> h.getProblem().getProblemId().equals(problemId))
                            .toList()
            );

            // 3. 대회의 문제 목록에서 제거
            List<Long> problemIds = contest.getProblemIds();
            if (problemIds != null) {
                problemIds.remove(problemId);
                contestJpaRepo.save(contest);
            }

            // 4. 테스트 케이스 삭제
            problemTestCaseJpaRepo.deleteAll(problemTestCaseJpaRepo.findByProblem_ProblemId(problemId));

            // 5. SavedCode 삭제 (저장된 코드)
            savedCodeJpaRepo.deleteByProblem_ProblemId(problemId);

            // 6. 문제 삭제 (CASCADE로 ContestSubmission 자동 삭제됨)
            problemJpaRepo.delete(problem);

            return Response.ok("대회 전용 문제가 성공적으로 삭제되었습니다.");

        } else {
            // ===== 가져온 일반 문제 (contestId == null) =====
            // 문제는 삭제하지 않고 대회와의 관계만 끊기

            // 1. ContestProblemMapping 삭제 (점수 매핑)
            contestProblemMappingJpaRepo.deleteByContest_CodeAndProblem_ProblemId(code, problemId);

            // 2. 대회의 문제 목록에서 제거
            List<Long> problemIds = contest.getProblemIds();
            if (problemIds != null) {
                problemIds.remove(problemId);
                contestJpaRepo.save(contest);
            }

            // 3. ContestProblemScore 삭제 (참가자들의 이 문제에 대한 점수)
            List<ContestParticipant> participants = contestParticipantJpaRepo
                    .findByContest_CodeOrderByTotalScoreDescTotalTimeSecondsAsc(code);

            for (ContestParticipant participant : participants) {
                contestProblemScoreJpaRepo.findByParticipant_IdAndProblem_ProblemId(
                        participant.getId(), problemId
                ).ifPresent(contestProblemScoreJpaRepo::delete);
            }

            return Response.ok("문제가 대회에서 제거되었습니다. (문제 자체는 삭제되지 않음)");
        }
    }

    // 대회 종료
    @Transactional
    public Response endContest(String code) {
        Contest contest = contestJpaRepo.findById(code)
                .orElseThrow(() -> new CustomException(ContestErrorCode.CONTEST_NOT_FOUND));

        contest.end();
        contestJpaRepo.save(contest);

        return Response.ok("대회가 성공적으로 종료되었습니다.");
    }

    // 관리자: 대회 참여자 목록 조회 (등수 순)
    @Transactional(readOnly = true)
    public List<ContestParticipantListRes> getContestParticipants(String code) {
        Contest contest = contestJpaRepo.findById(code)
                .orElseThrow(() -> new CustomException(ContestErrorCode.CONTEST_NOT_FOUND));

        List<ContestParticipant> participants = contestParticipantJpaRepo
                .findByContest_CodeOrderByTotalScoreDescTotalTimeSecondsAsc(code);

        List<Long> problemIds = contest.getProblemIds();
        List<Problem> problems = problemIds == null || problemIds.isEmpty()
                ? List.of()
                : problemJpaRepo.findAllById(problemIds);

        Integer rank = 1;
        List<ContestParticipantListRes> result = new ArrayList<>();

        for (ContestParticipant participant : participants) {
            User user = participant.getUser();
            List<ContestProblemScore> scores = contestProblemScoreJpaRepo.findByParticipant_Id(participant.getId());

            List<ContestParticipantListRes.ProblemScoreDetail> problemScores = new ArrayList<>();
            for (Problem problem : problems) {
                ContestProblemScore score = scores.stream()
                        .filter(s -> s.getProblem().getProblemId().equals(problem.getProblemId()))
                        .findFirst()
                        .orElse(null);

                problemScores.add(ContestParticipantListRes.ProblemScoreDetail.builder()
                        .problemId(problem.getProblemId())
                        .earnedScore(score != null ? score.getEarnedScore() : 0)
                        .maxScore(problem.getScore())
                        .build());
            }

            result.add(ContestParticipantListRes.builder()
                    .rank(rank++)
                    .userId(user.getId())
                    .nickname(user.getNickname())
                    .totalScore(participant.getTotalScore())
                    .totalTime(formatTime(participant.getTotalTimeSeconds()))
                    .problemScores(problemScores)
                    .build());
        }

        return result;
    }

    // 관리자: 참여자 점수 수정
    @Transactional
    public Response updateParticipantScore(String code, Long userId, ContestScoreUpdateReq req) {
        Contest contest = contestJpaRepo.findById(code)
                .orElseThrow(() -> new CustomException(ContestErrorCode.CONTEST_NOT_FOUND));

        ContestParticipant participant = contestParticipantJpaRepo.findByContest_CodeAndUser_Id(code, userId)
                .orElseThrow(() -> new CustomException(ContestErrorCode.PARTICIPANT_NOT_FOUND));

        Problem problem = problemJpaRepo.findById(req.problemId())
                .orElseThrow(() -> new CustomException(ProblemErrorCode.PROBLEM_NOT_FOUND));

        // 대회 전용 문제인지 확인
        if (!code.equals(problem.getContestId())) {
            throw new CustomException(ContestErrorCode.NOT_CONTEST_PROBLEM);
        }

        // 해당 문제의 점수 업데이트
        ContestProblemScore score = contestProblemScoreJpaRepo
                .findByParticipant_IdAndProblem_ProblemId(participant.getId(), req.problemId())
                .orElseGet(() -> ContestProblemScore.builder()
                        .participant(participant)
                        .problem(problem)
                        .earnedScore(0)
                        .timeSpentSeconds(0)
                        .build());

        score.updateScore(req.earnedScore());
        contestProblemScoreJpaRepo.save(score);

        // 총 점수 재계산
        List<ContestProblemScore> allScores = contestProblemScoreJpaRepo.findByParticipant_Id(participant.getId());
        Integer totalScore = allScores.stream()
                .map(ContestProblemScore::getEarnedScore)
                .reduce(0, Integer::sum);

        participant.updateTotalScore(totalScore);
        contestParticipantJpaRepo.save(participant);

        return Response.ok("점수가 성공적으로 수정되었습니다.");
    }

     // 특정 학생의 특정 문제 제출 코드 조회
    @Transactional(readOnly = true)
    public ContestSubmissionRes getContestSubmissionByUser(String code, Long problemId, Long userId) {
        // 대회 존재 확인
        contestJpaRepo.findById(code)
                .orElseThrow(() -> new CustomException(ContestErrorCode.CONTEST_NOT_FOUND));

        // 제출 기록 조회
        ContestSubmission submission = contestSubmissionJpaRepo
                .findFirstByContest_CodeAndUser_IdAndProblem_ProblemIdOrderBySubmittedAtDesc(code, userId, problemId)
                .orElseThrow(() -> new CustomException(ContestErrorCode.SUBMISSION_NOT_FOUND));

        // 테스트 케이스 조회
        List<ProblemTestCase> testCases = problemTestCaseJpaRepo.findByProblem_ProblemId(problemId);

        return ContestSubmissionRes.from(submission, testCases);
    }

    // 시간 포맷팅 (초 -> HH:MM:SS)
    private String formatTime(Integer seconds) {
        if (seconds == null || seconds == 0) return "00:00:00";
        Integer hours = seconds / 3600;
        Integer minutes = (seconds % 3600) / 60;
        Integer secs = seconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }

    private Integer difficultyToScore(DifficultyType difficulty) {
        if (difficulty == null) return 0;
        return switch (difficulty) {
            case COPPER -> 1;   // 구리
            case IRON -> 3;     // 철
            case SILVER -> 5;   // 은
            case GOLD -> 10;    // 금
            case JADE -> 15;    // 옥
        };
    }
}
