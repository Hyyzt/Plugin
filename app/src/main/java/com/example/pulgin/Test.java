package com.example.pulgin;

import com.the.data.TheData;
import com.the.data.TheDataBean;

import java.util.HashMap;

public class Test {

    public static void test(int userId,boolean isTrue){
        HashMap<String,String> hashMap = new HashMap<>();
        Integer mUserId = userId;
        hashMap.put("userId",mUserId.toString());
        TheData.INSTANCE.commit("2",0,hashMap);
    }
}
