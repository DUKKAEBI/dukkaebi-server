package com.ducami.dukkaebi.domain.contest.usecase;

import com.ducami.dukkaebi.domain.contest.domain.Contest;
import com.ducami.dukkaebi.domain.contest.domain.repo.ContestJpaRepo;
import com.ducami.dukkaebi.domain.contest.error.ContestErrorCode;
import com.ducami.dukkaebi.domain.contest.presentation.dto.request.ContestReq;
import com.ducami.dukkaebi.domain.contest.presentation.dto.response.ContestDetailRes;
import com.ducami.dukkaebi.domain.contest.presentation.dto.response.ContestListRes;
import com.ducami.dukkaebi.domain.contest.util.CodeGenerator;
import com.ducami.dukkaebi.global.common.Response;
import com.ducami.dukkaebi.global.exception.CustomException;
import com.ducami.dukkaebi.global.security.auth.UserSessionHolder;
import com.ducami.dukkaebi.domain.problem.domain.Problem;
import com.ducami.dukkaebi.domain.problem.domain.ProblemHistory;
import com.ducami.dukkaebi.domain.problem.domain.repo.ProblemHistoryJpaRepo;
import com.ducami.dukkaebi.domain.problem.domain.repo.ProblemJpaRepo;
import com.ducami.dukkaebi.domain.problem.presentation.dto.response.ProblemRes;
import java.time.LocalDate;
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
    private final CodeGenerator codeGenerator;
    private final UserSessionHolder userSessionHolder;
    private final ProblemJpaRepo problemJpaRepo;
    private final ProblemHistoryJpaRepo problemHistoryJpaRepo;

    private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");

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

    @Transactional(readOnly = true)
    public java.util.List<ContestListRes> getContestList() {
        Long userId = userSessionHolder.getUserId();

        return contestJpaRepo.findAllByOrderByEndDateAsc()
                .stream()
                .map(contest -> ContestListRes.from(contest, userId))
                .toList();
    }

    @Transactional
    public Response joinContest(String code) {
        Contest contest = contestJpaRepo.findById(code)
                .orElseThrow(() -> new CustomException(ContestErrorCode.CONTEST_NOT_FOUND));

        LocalDate today = LocalDate.now(ZONE);
        if (contest.getEndDate().isBefore(today)) {
            return Response.of(HttpStatus.BAD_REQUEST, "대회가 종료되었습니다.");
        }

        Long userId = userSessionHolder.getUserId();
        if (contest.getParticipantIds().contains(userId)) {
            return Response.ok("이미 참여중입니다.");
        }

        contest.addParticipant(userId);
        contestJpaRepo.save(contest);
        return Response.ok("대회에 참가하였습니다.");
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

        return ContestDetailRes.from(contest, problemResList);
    }
}