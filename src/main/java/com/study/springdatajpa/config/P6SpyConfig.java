package com.study.springdatajpa.config;

import com.p6spy.engine.logging.Category;
import com.p6spy.engine.spy.P6SpyOptions;
import com.p6spy.engine.spy.appender.MessageFormattingStrategy;
import jakarta.annotation.PostConstruct;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.hibernate.engine.jdbc.internal.FormatStyle;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ClassUtils;

// p6spy가 실제 JDBC로 나가는 모든 SQL을 가로챌 때 쓸 출력 포맷을 커스텀한다.
@Configuration
public class P6SpyConfig implements MessageFormattingStrategy {

    // 앱 구동 시 p6spy에게 "포맷은 이 클래스가 담당한다"고 등록.
    @PostConstruct
    public void setLogMessageFormat() {
        P6SpyOptions.getActiveInstance().setLogMessageFormat(this.getClass().getName());
    }

    // p6spy가 SQL 하나 실행할 때마다 호출하는 콜백. 실제 로그 한 줄의 최종 형태를 만든다.
    @Override
    public String formatMessage(int connectionId, String now, long elapsed, String category,
        String prepared, String sql, String url) {
        return String.format("[%s] | %d ms | %s", category, elapsed,
            formatSql(category, sql));
    }

    // 이 SQL을 호출한 우리 코드 쪽 스택트레이스만 뽑아서 로그에 같이 붙여준다.
    private String stackTrace() {
        return Stream.of(new Throwable().getStackTrace())
            .filter(t -> t.toString().startsWith("com.study.springdatajpa") && !t.toString().contains(
                ClassUtils.getUserClass(this).getName()))
            .map(StackTraceElement::toString)
            .collect(Collectors.joining("\n"));
    }

    // DDL(create/alter/comment)은 DDL 포맷, 나머지는 BASIC(줄바꿈)으로 정렬 후
    // HIGHLIGHT(키워드 색상)를 입힌다. HIGHLIGHT 단독으로는 줄바꿈이 안 되기 때문에 체이닝함.
    private String formatSql(String category, String sql) {
        if (sql != null && !sql.trim().isEmpty() && Category.STATEMENT.getName().equals(category)) {
            String trimmedSql = sql.trim().toLowerCase(Locale.ROOT);
            return stackTrace() + (trimmedSql.startsWith("create") || trimmedSql.startsWith("alter")
                || trimmedSql.startsWith("comment")
                ? FormatStyle.DDL.getFormatter().format(sql)
                : FormatStyle.HIGHLIGHT.getFormatter()
                    .format(FormatStyle.BASIC.getFormatter().format(sql)));
        }
        return sql;
    }
}
