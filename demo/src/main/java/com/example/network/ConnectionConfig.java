package com.example.network;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class ConnectionConfig {
    private static final String CONFIG_FILE = 
        System.getProperty("user.home") + "/.tetris/recent_ips.json";
    
    private static final int MAX_RECENT_IPS = 5;  // 최대 5개 저장
    
    //최근 IP 목록 불러오기
    public static List<String> loadRecentIPs() {
        List<String> ips = new ArrayList<>();
        
        try {
            if (Files.exists(Paths.get(CONFIG_FILE))) {
                String content = new String(Files.readAllBytes(Paths.get(CONFIG_FILE)));
                JSONObject json = new JSONObject(content);
                JSONArray array = json.getJSONArray("recent_ips");
                
                for (int i = 0; i < array.length(); i++) {
                    ips.add(array.getString(i));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return ips;
    }
    
    //IP 추가 및 저장
    public static void saveRecentIP(String ip) {
        List<String> ips = loadRecentIPs();
        
        // 이미 있으면 제거 (맨 위로 올리기 위해)
        ips.remove(ip);
        
        // 맨 앞에 추가
        ips.add(0, ip);
        
        // 최대 개수 제한
        if (ips.size() > MAX_RECENT_IPS) {
            ips = ips.subList(0, MAX_RECENT_IPS);
        }
        
        // JSON으로 저장
        try {
            Files.createDirectories(Paths.get(CONFIG_FILE).getParent());
            
            JSONObject json = new JSONObject();
            json.put("recent_ips", new JSONArray(ips));
            
            Files.write(Paths.get(CONFIG_FILE), 
                       json.toString(2).getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}