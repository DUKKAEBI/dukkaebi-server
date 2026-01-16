package com.ducami.dukkaebi.domain.contest.domain.repo;

import com.ducami.dukkaebi.domain.contest.domain.ContestSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContestSubmissionJpaRepo extends JpaRepository<ContestSubmission, Long> {
    // 대회별 특정 사용자와 문제에 대한 제출 기록 조회 (최신순)
    Optional<ContestSubmission> findFirstByContest_CodeAndUser_IdAndProblem_ProblemIdOrderBySubmittedAtDesc(
            String contestCode, Long userId, Long problemId);

    // 대회별 모든 제출 기록 조회 (학생별로 각 문제의 최종 제출만)
    @Query("SELECT cs FROM ContestSubmission cs WHERE cs.contest.code = :contestCode " +
            "AND cs.id IN (" +
            "  SELECT MAX(cs2.id) FROM ContestSubmission cs2 " +
            "  WHERE cs2.contest.code = :contestCode " +
            "  GROUP BY cs2.user.id, cs2.problem.problemId" +
            ")")
    List<ContestSubmission> findLatestSubmissionsByContest(@Param("contestCode") String contestCode);

    // 대회별 특정 문제에 대한 모든 최종 제출 조회
    @Query("SELECT cs FROM ContestSubmission cs WHERE cs.contest.code = :contestCode " +
            "AND cs.problem.problemId = :problemId " +
            "AND cs.id IN (" +
            "  SELECT MAX(cs2.id) FROM ContestSubmission cs2 " +
            "  WHERE cs2.contest.code = :contestCode " +
            "  AND cs2.problem.problemId = :problemId " +
            "  GROUP BY cs2.user.id" +
            ")")
    List<ContestSubmission> findLatestSubmissionsByContestAndProblem(
            @Param("contestCode") String contestCode,
            @Param("problemId") Long problemId);

    // 사용자의 모든 제출 기록 삭제
    int deleteByUser_Id(Long userId);
}

