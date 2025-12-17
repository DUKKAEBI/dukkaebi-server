package com.ducami.dukkaebi.domain.user.service;

import com.ducami.dukkaebi.domain.user.domain.User;
import com.ducami.dukkaebi.domain.user.domain.enums.UserType;
import com.ducami.dukkaebi.domain.user.presentation.dto.request.UserFilterReq;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Service
public class UserFilterService {

    /**
     * 사용자 목록을 필터링하고 정렬합니다.
     */
    public List<User> filterAndSortUsers(
            List<User> users,
            UserFilterReq filter
    ) {
        Stream<User> stream = users.stream()
                .filter(user -> user.getRole() == UserType.STUDENT);

        // 키워드 필터링 (닉네임 또는 로그인 아이디)
        if (filter.keyword() != null && !filter.keyword().isBlank()) {
            String keyword = filter.keyword().toLowerCase().trim();
            stream = stream.filter(user ->
                    user.getNickname().toLowerCase().contains(keyword) ||
                            user.getLoginId().toLowerCase().contains(keyword)
            );
        }

        // 정렬 (오름차순)
        Comparator<User> comparator = getComparator(filter.sortBy());

        return stream.sorted(comparator).toList();
    }

    /**
     * 정렬 기준에 따른 Comparator 반환
     */
    private Comparator<User> getComparator(UserFilterReq.SortType sortType) {
        return switch (sortType) {
            case NICKNAME -> Comparator.comparing(User::getNickname, String.CASE_INSENSITIVE_ORDER);
            case LOGIN_ID -> Comparator.comparing(User::getLoginId, String.CASE_INSENSITIVE_ORDER);
            case GROWTH -> Comparator.comparing(User::getGrowth);
        };
    }
}