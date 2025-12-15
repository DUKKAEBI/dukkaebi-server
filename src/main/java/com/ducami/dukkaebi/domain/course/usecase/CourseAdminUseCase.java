package com.ducami.dukkaebi.domain.course.usecase;

import com.ducami.dukkaebi.domain.course.domain.repo.CourseJpaRepo;
import com.ducami.dukkaebi.domain.course.error.CourseErrorCode;
import com.ducami.dukkaebi.domain.course.presentation.dto.request.CourseReq;
import com.ducami.dukkaebi.global.common.Response;
import com.ducami.dukkaebi.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CourseAdminUseCase {
    private final CourseJpaRepo courseJpaRepo;

    @Transactional
    public Response createCourse(CourseReq req) {
        if (courseJpaRepo.existsByTitle(req.title())) {
            throw new CustomException(CourseErrorCode.TITLE_ALREADY);
        }

        courseJpaRepo.save(CourseReq.fromReq(req));

        return Response.created("코스가 성공적으로 생성되었습니다.");
    }
}
