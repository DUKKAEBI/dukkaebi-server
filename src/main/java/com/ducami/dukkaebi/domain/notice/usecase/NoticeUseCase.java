package com.ducami.dukkaebi.domain.notice.usecase;

import com.ducami.dukkaebi.domain.notice.domain.Notice;
import com.ducami.dukkaebi.domain.notice.domain.repo.NoticeJpaRepo;
import com.ducami.dukkaebi.domain.notice.error.NoticeErrorCode;
import com.ducami.dukkaebi.domain.notice.presentation.dto.request.NoticeReq;
import com.ducami.dukkaebi.domain.notice.presentation.dto.response.NoticeDetailRes;
import com.ducami.dukkaebi.domain.notice.presentation.dto.response.NoticeListRes;
import com.ducami.dukkaebi.domain.user.domain.User;
import com.ducami.dukkaebi.global.common.dto.response.PageResponse;
import com.ducami.dukkaebi.global.common.dto.response.Response;
import com.ducami.dukkaebi.global.exception.CustomException;
import com.ducami.dukkaebi.global.security.auth.UserSessionHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NoticeUseCase {
    private final NoticeJpaRepo noticeJpaRepo;
    private final UserSessionHolder userSessionHolder;

    @Transactional(readOnly = true)
    public PageResponse<NoticeListRes> getNoticeListPaged(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<NoticeListRes> noticePage = noticeJpaRepo.findAll(pageable)
                .map(NoticeListRes::from);
        return PageResponse.of(noticePage);
    }

    @Transactional(readOnly = true)
    public List<NoticeListRes> searchNotices(String keyword) {
        return noticeJpaRepo.searchByKeyword(keyword)
                .stream().map(NoticeListRes::from).toList();
    }

    @Transactional
    public NoticeDetailRes getNoticeDetail(Long noticeId) {
        Notice notice = noticeJpaRepo.findById(noticeId)
                .orElseThrow(() -> new CustomException(NoticeErrorCode.NOTICE_NOT_FOUND));

        // 조회수 증가
        notice.incrementViewCount();

        return NoticeDetailRes.from(notice);
    }

    @Transactional
    public Response createNotice(NoticeReq req) {
        // 작성자 정보
        User author = userSessionHolder.getUser();

        // 공지사항 저장
        Notice notice = NoticeReq.toEntity(req, author);
        noticeJpaRepo.save(notice);

        return Response.created("공지사항이 성공적으로 생성되었습니다.");
    }

    @Transactional
    public Response updateNotice(Long noticeId, NoticeReq req) {
        Notice notice = noticeJpaRepo.findById(noticeId)
                .orElseThrow(() -> new CustomException(NoticeErrorCode.NOTICE_NOT_FOUND));

        // 기본 정보 수정
        notice.updateNotice(req.title(), req.content(), req.fileUrl());

        return Response.ok("공지사항이 성공적으로 수정되었습니다.");
    }

    @Transactional
    public Response deleteNotice(Long noticeId) {
        Notice notice = noticeJpaRepo.findById(noticeId)
                .orElseThrow(() -> new CustomException(NoticeErrorCode.NOTICE_NOT_FOUND));

        noticeJpaRepo.delete(notice);

        return Response.ok("공지사항이 성공적으로 삭제되었습니다.");
    }
}
