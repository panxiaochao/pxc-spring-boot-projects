package io.github.test.controller;

import io.github.panxiaochao.boot3.common.response.R;
import io.github.panxiaochao.boot3.holiday.core.HolidayClient;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>节假日测试</p>
 *
 * @author Lypxc
 * @version 1.0
 * @since 2024-04-03
 */
@RestController
@RequestMapping("/holiday")
@RequiredArgsConstructor
public class HolidayController {

    private final HolidayClient holidayClient;

    @GetMapping
    public R<Boolean> isHoliday() {
        return R.ok(holidayClient.isHoliday("2024-04-03"));
    }
}
