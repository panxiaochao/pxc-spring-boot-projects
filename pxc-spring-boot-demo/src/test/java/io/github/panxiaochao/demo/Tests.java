package io.github.panxiaochao.demo;

import cn.hutool.system.oshi.OshiUtil;
import org.springframework.util.CollectionUtils;
import oshi.util.GlobalConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * <p></p>
 *
 * @author Lypxc
 * @since 2023-08-16
 */
public class Tests {
    public static void main(String[] args) {
        //test1();
//        test2();
        GlobalConfig.set(GlobalConfig.OSHI_OS_WINDOWS_CPU_UTILITY, true);
        System.out.println(OshiUtil.getCpuInfo().toString());
    }

    static void test1() {
        List<String> openIDs = new ArrayList<>();
        for (int i = 0; i < 100001; i++) {
            openIDs.add(i + "");
        }
        List<List<String>> openIdSlices = IntStream.range(0, (openIDs.size() + 9999) / 10000)
                .mapToObj(i -> openIDs.subList(i * 10000, Math.min((i + 1) * 10000, openIDs.size())))
                .collect(Collectors.toList());
        //处理推送给10001个人，最后一个人收不到的情况
        if (openIdSlices.size() > 1 && openIdSlices.get(openIdSlices.size() - 1).size() == 1) {
            // 获取倒数第二个数组
            List<String> openIdTempLast = new ArrayList<>(openIdSlices.get(openIdSlices.size() - 1));
            List<String> openIdTempLast2 = new ArrayList<>(openIdSlices.get(openIdSlices.size() - 2));
            // 如果倒数第二个分片已经满10000个人，则反向移动倒数第二分片的最后一个openid到最后一个分片
            openIdTempLast.add(openIdTempLast2.get(openIdTempLast2.size() - 1));
            // 赋值
            openIdSlices.set(openIdSlices.size() - 1, openIdTempLast);
            openIdSlices.set(openIdSlices.size() - 2, openIdTempLast2.subList(0, openIdTempLast2.size() - 1));
        }

        for (List<String> openIdSlice : openIdSlices) {
            System.out.println(openIdSlice.size());
        }
    }

    public static void test2() {
        // 模拟数据
        List<String> openIds = new ArrayList<>();
        for (int i = 0; i < 100001; i++) {
            openIds.add((i + 1) + "");
        }
        Map<Integer, List<String>> listMap = splitList(openIds, 10000);
        System.out.println();
        // 输出结果
        for (Map.Entry<Integer, List<String>> listEntry : listMap.entrySet()) {
            System.out.println(listEntry.getKey() + "=" + listEntry.getValue());
        }
    }

    public static Map<Integer, List<String>> splitList(List<String> list, int pageSize) {
        Map<Integer, List<String>> listMap = new HashMap<>();
        // 数据不为空的情况下发生
        if (!CollectionUtils.isEmpty(list)) {
            int total = list.size();
            int cur = 0;
            // 取模
            int mod = total % pageSize;
            int totalPages = mod != 0 ? (total / pageSize) + 1 : (total / pageSize);
            // 是否需要分割，针对多出1条数据的情况
            boolean isSplit = (mod == 1);
            for (int i = 0; i < totalPages; i++) {
                int lastIndex = Math.min((cur + pageSize), total);
                // 是否需要分割, 从倒数第二个开始
                if (isSplit && (totalPages - 2) == i) {
                    listMap.put(cur, list.subList(cur, lastIndex - 1));
                    cur = (i + 1) * pageSize - 1;
                } else {
                    listMap.put(cur, list.subList(cur, lastIndex));
                    cur = (i + 1) * pageSize;
                }

            }
        }
        return listMap;
    }
}
