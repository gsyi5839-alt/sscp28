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
                        .title("新春祝福")
                        .content("尊敬的会员您好！值此马年新春之际，谨向一直以来支持与信赖我们的广大用户朋友致以衷心感谢和新春祝福！ 新的一年，我们将持续提升系统稳定性与服务效率，优化产品体验，为您提供更加安全、便捷、优质的服务保障。 感谢您一直以来对本系统的支持！请认准本系统(18118bw.com,18118bw.cc)开奖网(bw128.cc)")
                        .createTime(LocalDate.of(2026, 2, 27))
                        .enabled(true)
                        .build(),
                Notice.builder()
                        .category("特别通知")
                        .title("防骗提醒")
                        .content("尊敬的会员您好，当心市场冒充老BW这类骗局，请认准本系统(18118bw.com,18118bw.cc)开奖网(bw128.cc)")
                        .createTime(LocalDate.of(2026, 1, 20))
                        .enabled(true)
                        .build(),
                Notice.builder()
                        .category("特别通知")
                        .title("新游戏上线")
                        .content("尊敬的会员，您好！为了公平公正的原则，以及更好的游戏氛围及体验，本系统新添加官方游戏，加拿大PC28和加拿大时时彩，开奖数据由加拿大官方提供(https://lotto.bclc.com )同时每个游戏添加新玩法（宝斗，牛牛，斗牛）")
                        .createTime(LocalDate.of(2024, 9, 14))
                        .enabled(true)
                        .build(),
                Notice.builder()
                        .category("通知")
                        .title("账号安全")
                        .content("请各位会员注意保管好自己的账号密码，不要向任何人透露您的账户信息。")
                        .createTime(LocalDate.of(2026, 2, 15))
                        .enabled(true)
                        .build(),
                Notice.builder()
                        .category("安全通知")
                        .title("密码安全")
                        .content("为了保障您的账户安全，建议定期修改密码，并开启双重验证。")
                        .createTime(LocalDate.of(2026, 2, 10))
                        .enabled(true)
                        .build(),
                Notice.builder()
                        .category("站点通知")
                        .title("欢迎公告")
                        .content("欢迎访问本站，祝您使用愉快！如有任何问题请联系在线客服。")
                        .createTime(LocalDate.of(2026, 2, 1))
                        .enabled(true)
                        .build()
        );

        noticeRepository.saveAll(notices);
        log.info("Seeded {} notices successfully", notices.size());
    }
}
