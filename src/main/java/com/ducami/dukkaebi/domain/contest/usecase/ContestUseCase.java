package com.ducami.dukkaebi.domain.contest.usecase;

import com.ducami.dukkaebi.domain.contest.domain.Contest;
import com.ducami.dukkaebi.domain.contest.domain.ContestParticipant;
import com.ducami.dukkaebi.domain.contest.domain.ContestProblemScore;
import com.ducami.dukkaebi.domain.contest.domain.repo.ContestJpaRepo;
import com.ducami.dukkaebi.domain.contest.domain.repo.ContestParticipantJpaRepo;
import com.ducami.dukkaebi.domain.contest.domain.repo.ContestProblemScoreJpaRepo;
import com.ducami.dukkaebi.domain.contest.error.ContestErrorCode;
import com.ducami.dukkaebi.domain.contest.presentation.dto.request.ContestReq;
import com.ducami.dukkaebi.domain.contest.presentation.dto.request.ContestScoreUpdateReq;
import com.ducami.dukkaebi.domain.contest.presentation.dto.response.ContestDetailRes;
import com.ducami.dukkaebi.domain.contest.presentation.dto.response.ContestListRes;
import com.ducami.dukkaebi.domain.contest.presentation.dto.response.ContestParticipantListRes;
import com.ducami.dukkaebi.domain.contest.util.CodeGenerator;
import com.ducami.dukkaebi.domain.problem.error.ProblemErrorCode;
import com.ducami.dukkaebi.domain.user.domain.User;
import com.ducami.dukkaebi.global.common.Response;
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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ContestUseCase {
    private final ContestJpaRepo contestJpaRepo;
    private final ContestParticipantJpaRepo contestParticipantJpaRepo;
    private final ContestProblemScoreJpaRepo contestProblemScoreJpaRepo;
    private final CodeGenerator codeGenerator;
    private final UserSessionHolder userSessionHolder;
    private final ProblemJpaRepo problemJpaRepo;
    private final ProblemHistoryJpaRepo problemHistoryJpaRepo;
    private final ProblemTestCaseJpaRepo problemTestCaseJpaRepo;

    private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");

    @Transactional(readOnly = true)
    public java.util.List<ContestListRes> getContestList() {
        Long userId = userSessionHolder.getUserId();

        return contestJpaRepo.findAllByOrderByEndDateAsc()
                .stream()
                .map(contest -> ContestListRes.from(contest, userId))
                .toList();
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
            problemResList.add(ProblemRes.from(p, history));
        }

        return ContestDetailRes.from(contest, problemResList, userId);
    }

    @Transactional(readOnly = true)
    public List<ContestListRes> getContestWithName(String name) {
        Long userId = userSessionHolder.getUserId();

        return contestJpaRepo.findByTitleContainingIgnoreCaseOrderByEndDateAsc(name)
                .stream()
                .map(contest -> ContestListRes.from(contest, userId))
                .toList();
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

        contest.updateContest(req.title(), req.description(), req.startDate(), req.endDate());
        return Response.ok("대회가 성공적으로 수정되었습니다.");
    }

    @Transactional
    public Response deleteContest(String code) {
        Contest contest = contestJpaRepo.findById(code)
                .orElseThrow(() -> new CustomException(ContestErrorCode.CONTEST_NOT_FOUND));

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

    // 대회 문제 삭제
    @Transactional
    public Response deleteContestProblem(String code, Long problemId) {
        Contest contest = contestJpaRepo.findById(code)
                .orElseThrow(() -> new CustomException(ContestErrorCode.CONTEST_NOT_FOUND));

        Problem problem = problemJpaRepo.findById(problemId)
                .orElseThrow(() -> new CustomException(ProblemErrorCode.PROBLEM_NOT_FOUND));

        // 대회 전용 문제인지 확인
        if (!code.equals(problem.getContestId())) {
            throw new CustomException(ContestErrorCode.NOT_CONTEST_PROBLEM);
        }

        // 대회의 문제 목록에서 제거
        List<Long> problemIds = contest.getProblemIds();
        if (problemIds != null) {
            problemIds.remove(problemId);
            contestJpaRepo.save(contest);
        }

        // 문제 및 테스트 케이스 삭제
        problemTestCaseJpaRepo.deleteAll(problemTestCaseJpaRepo.findByProblem_ProblemId(problemId));
        problemJpaRepo.delete(problem);

        return Response.ok("대회 문제가 성공적으로 삭제되었습니다.");
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

    // 시간 포맷팅 (초 -> HH:MM:SS)
    private String formatTime(Integer seconds) {
        if (seconds == null || seconds == 0) return "00:00:00";
        Integer hours = seconds / 3600;
        Integer minutes = (seconds % 3600) / 60;
        Integer secs = seconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }
}
