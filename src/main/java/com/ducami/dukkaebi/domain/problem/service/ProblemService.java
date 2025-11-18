package com.ducami.dukkaebi.domain.problem.service;

import com.ducami.dukkaebi.domain.problem.domain.Problem;
import com.ducami.dukkaebi.domain.problem.domain.repo.ProblemJpaRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProblemService {

    private final ProblemJpaRepo problemJpaRepo;

    public List<Problem> getAllProblems() {
        log.info("모든 문제 조회 시작");
        List<Problem> problems = problemJpaRepo.findAll();
        log.info("문제 조회 완료 - 총 {}개", problems.size());
        return problems;
    }
}
