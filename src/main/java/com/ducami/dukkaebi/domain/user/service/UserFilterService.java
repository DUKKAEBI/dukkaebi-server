package com.ducami.dukkaebi.domain.user.service;

import com.ducami.dukkaebi.domain.user.domain.User;
import com.ducami.dukkaebi.domain.user.domain.enums.SortType;
import com.ducami.dukkaebi.domain.user.domain.enums.UserType;
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
            String keyword,
            SortType sortType
    ) {
        Stream<User> stream = users.stream()
                .filter(user -> user.getRole() == UserType.STUDENT);

        // 키워드 필터링 (닉네임 또는 로그인 아이디)
        if (keyword != null && !keyword.isBlank()) {
            String searchKeyword = keyword.toLowerCase().trim();
            stream = stream.filter(user ->
                    user.getNickname().toLowerCase().contains(searchKeyword) ||
                            user.getLoginId().toLowerCase().contains(searchKeyword)
            );
        }

        // 정렬 (오름차순)
        // sortType이 null이면 기본값 NICKNAME 사용
        SortType actualSortType = sortType != null ? sortType : SortType.NICKNAME;
        Comparator<User> comparator = getComparator(actualSortType);

        return stream.sorted(comparator).toList();
    }

    /**
     * 정렬 기준에 따른 Comparator 반환
     */
    private Comparator<User> getComparator(SortType sortType) {
        return switch (sortType) {
            case NICKNAME -> Comparator.comparing(User::getNickname, String.CASE_INSENSITIVE_ORDER);
            case LOGIN_ID -> Comparator.comparing(User::getLoginId, String.CASE_INSENSITIVE_ORDER);
            case GROWTH -> Comparator.comparing(User::getGrowth);
        };
    }
}