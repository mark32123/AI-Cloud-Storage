package net.xdclass.util;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * @author 雨过天晴
 * @date 2026/8/15
 * @Description
 */
public class test {

    public static void main(String[] args) {

        TreeMap<String, String> treeMap = new TreeMap<>();

        treeMap.put("1号", "王俊");
        treeMap.put("2号", "里俊");
        treeMap.put("3号", "找俊");
        treeMap.put("4号", "陈俊");

        // 3. 获取
        Set<Map.Entry<String, String>> set = treeMap.entrySet();

        // 4. 遍历
        System.out.println("遍历 TreeMap：");
        for (Map.Entry<String, String> entry : set) {
            System.out.println(entry.getKey() + " : " + entry.getValue());
        }
    }
}
