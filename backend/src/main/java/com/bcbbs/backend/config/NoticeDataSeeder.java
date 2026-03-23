package com.bcbbs.backend.config;

import com.bcbbs.backend.entity.Notice;
import com.bcbbs.backend.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * Seeds initial notice data into the database on application startup.
 * Only inserts data when the notices table is empty (first-time setup).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NoticeDataSeeder implements ApplicationRunner {

    private final NoticeRepository noticeRepository;

    @Override
    public void run(ApplicationArguments args) {
        if (noticeRepository.count() > 0) {
            log.debug("Notices table already has data, skipping seed");
            return;
        }

        log.info("Seeding initial notice data...");

        List<Notice> notices = List.of(
                Notice.builder()
                        .category("特别通知")
                        .title("特别通知")
                        .content("尊敬的会员您好，当心市场冒充老BW这类骗局，请认准本系统(18118bw.com,18118bw.cc)开奖网(bw128.cc)")
                        .createTime(LocalDate.of(2026, 2, 6))
                        .enabled(true)
                        .build(),
                Notice.builder()
                        .category("特别通知")
                        .title("系统维护通知")
                        .content("为了给您提供更好的服务体验，系统将于今晚23:00-24:00进行维护升级，期间可能无法访问，请您谅解。")
                        .createTime(LocalDate.of(2026, 2, 5))
                        .enabled(true)
                        .build(),
                Notice.builder()
                        .category("通知")
                        .title("重要通知")
                        .content("请各位会员注意保管好自己的账号密码，不要向任何人透露您的账户信息。")
                        .createTime(LocalDate.of(2026, 2, 4))
                        .enabled(true)
                        .build(),
                Notice.builder()
                        .category("安全通知")
                        .title("账户安全提示")
                        .content("为了保障您的账户安全，建议定期修改密码，并开启双重验证。")
                        .createTime(LocalDate.of(2026, 2, 3))
                        .enabled(true)
                        .build(),
                Notice.builder()
                        .category("站点通知")
                        .title("站点公告")
                        .content("欢迎访问本站，祝您使用愉快！")
                        .createTime(LocalDate.of(2026, 2, 2))
                        .enabled(true)
                        .build()
        );

        noticeRepository.saveAll(notices);
        log.info("Seeded {} notices successfully", notices.size());
    }
}
