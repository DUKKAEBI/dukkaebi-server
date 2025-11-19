package com.ducami.dukkaebi.domain.contest.usecase;

import com.ducami.dukkaebi.domain.contest.domain.repo.ContestJpaRepo;
import com.ducami.dukkaebi.domain.contest.error.ContestErrorCode;
import com.ducami.dukkaebi.domain.contest.presentation.dto.request.ContestReq;
import com.ducami.dukkaebi.domain.contest.util.CodeGenerator;
import com.ducami.dukkaebi.global.common.Response;
import com.ducami.dukkaebi.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ContestUseCase {
    private final ContestJpaRepo contestJpaRepo;
    private final CodeGenerator codeGenerator;

    public Response createContest(ContestReq req) {
        String code = codeGenerator.generateCode();

        if(contestJpaRepo.existsByTitle(req.title())) {
            throw new CustomException(ContestErrorCode.TITLE_ALREADY);
        }

        while (contestJpaRepo.findById(code).isPresent()) {
            code = codeGenerator.generateCode();
        }

        contestJpaRepo.save(ContestReq.fromReq(code, req));

        return Response.created("대회가 성공적으로 생성되었습니다.");
    }
}
