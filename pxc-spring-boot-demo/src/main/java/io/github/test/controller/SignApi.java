// package io.github.test.controller;
//
// import io.github.panxiaochao.boot3.common.response.R;
// import io.github.panxiaochao.boot3.redis.utils.RedissonUtil;
// import io.github.panxiaochao.boot3.utils.date.LocalDateTimeUtil;
// import io.swagger.v3.oas.annotations.tags.Tag;
// import lombok.RequiredArgsConstructor;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RestController;
//
// import java.time.LocalDate;
// import java.util.Arrays;
// import java.util.BitSet;
//
// /**
//  * <p>签到Api</p>
//  * <p>
//  * 说明：
//  *     <ul>
//  *         <li>可以当天签到</li>
//  *         <li>统计本月签到天数</li>
//  *         <li>统计本月连续签到天数</li>
//  *     </ul>
//  * </p>
//  *
//  * @author Lypxc
//  * @version 1.0
//  * @since 2024-04-16
//  */
// @Tag(name = "签到Api", description = "签到Api")
// @RestController
// @RequestMapping("/api/sign")
// @RequiredArgsConstructor
// public class SignApi {
//
//     /**
//      * 格式：user_sign_in:用户ID:年:月
//      */
//     private static final String SIGN_KEY = "user_sign_in:%s:%s:%s";
//
//     /**
//      * 签到
//      */
//     @GetMapping("/signIn")
//     public R<Object> signIn(String userId) {
//         LocalDate localDate = LocalDate.now();
//         int day = localDate.getDayOfMonth();
//         String redisSignKey = getSignKey(userId, localDate);
//         boolean flag = RedissonUtil.addBit(redisSignKey, day);
//         if (!flag) {
//             return R.ok();
//         }
//         return R.fail("您已签到");
//     }
//
//     /**
//      * 通过日期补签
//      */
//     @GetMapping("/signSupplementary")
//     public R<Object> signSupplementary(String userId, String date) {
//         LocalDate localDate = LocalDateTimeUtil.stringToLocalDate(date);
//         int day = localDate.getDayOfMonth();
//         String redisSignKey = getSignKey(userId, localDate);
//         boolean flag = RedissonUtil.addBit(redisSignKey, day);
//         if (!flag) {
//             return R.ok();
//         }
//         return R.fail("您已补签");
//     }
//
//     /**
//      * 本月签到天数
//      */
//     @GetMapping("/signCount")
//     public R<Object> signCount(String userId) {
//         String redisSignKey = getSignKey(userId, LocalDate.now());
//         return R.ok(RedissonUtil.getBitCardinality(redisSignKey));
//     }
//
//     /**
//      * 本月连续签到
//      * <p>首先需要将今天的签到和昨天开始的连续签到分开计算，例如一周中，星期天没有签到，那么连续签到是6天，如果周天签到了，那就是7天</p>
//      */
//     @GetMapping("/signContinuous")
//     public R<Object> continuousSign(String userId) {
//         String redisSignKey = getSignKey(userId, LocalDate.now());
//         BitSet bitSet = RedissonUtil.getBit(redisSignKey);
//         if (bitSet.isEmpty()) {
//             return R.fail("没有签到数据");
//         }
//         LocalDate localDate = LocalDate.now();
//         final int day = localDate.getDayOfMonth();
//         int[] days = bitSet.stream().filter(f -> f <= day).toArray();
//         // 首先判断今天有没有签到
//         boolean todaySign = Arrays.stream(days).anyMatch(f -> (f == day));
//         int continuousSignDay = 0;
//         // 从昨天开始计算
//         int lastDay = day;
//         for (int i = days.length - 2; i >= 0; i--) {
//             lastDay--;
//             if (days[i] == lastDay) {
//                 continuousSignDay++;
//             } else {
//                 break;
//             }
//         }
//         // 今天签到了的话，加上今天
//         if (todaySign) {
//             continuousSignDay++;
//         }
//         System.out.println(RedissonUtil.countKeys());
//         System.out.println(RedissonUtil.getKeysByPattern("user_sign_in*"));
//         return R.ok(continuousSignDay);
//     }
//
//     private String getSignKey(String userId, LocalDate localDate) {
//         int year = localDate.getYear();
//         int month = localDate.getMonthValue();
//         return String.format(SIGN_KEY, userId, year, month);
//     }
// }
