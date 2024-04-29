package com.sharp.common.utils;
import kr.co.shineware.nlp.komoran.constant.DEFAULT_MODEL;
import kr.co.shineware.nlp.komoran.core.Komoran;
import kr.co.shineware.nlp.komoran.model.KomoranResult;
import org.json.JSONException;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class KomoranNlp {
    public List<Map<String, Object>> morphologicalAnalyzer(String target) throws Exception {
        try {
            // Komoran 사용을 위한 초기화 && 선언
            Komoran komoran = new Komoran(DEFAULT_MODEL.FULL);

            // 가져온 String을 komoran analyze 메소드에 넣기
            KomoranResult analyzeResultList = komoran.analyze(target);

            // 여기서 getMorphesByTags 사용하면 내가원하는 형태소만 뽑아낼 수 있음
            List<String> analyzeList = analyzeResultList.getMorphesByTags("NNP", "NNG", "NNB", "NP");

            // list 파일로 떨어진 analyzeList 를 HashMap 에 넣어서 중복된 데이터를 삭제하고
            // Conllections.frequency 를 사용해서 몇 번이나 중복되었는지 분석하여 저장한다.
            // 최종적으로 listHash 에는 단어=중복횟수 로 저장된다.
            // Collections.frequency(Collections객체, 값)
            HashMap<String, Integer> listHash = new HashMap<>();

            for (String l : analyzeList) {
                int num = Collections.frequency(analyzeList, l);
                listHash.put(l, num);
            }

            Map<String, Integer> result = sortMapByValue(listHash);

            List<Map<String, Object>> komoranMapList = new ArrayList<>();

            int index = 0;

            for(String list : result.keySet()){
                Map<String, Object> informationObject = new HashMap<>();
                // JsonArray 에 저장하기 위해서 값을 하나씩 json 형태로 가져와서 text 에 key를 담고 , weight  에는 value을 저장함
                // 이때 anychart 의 경우 x 와 value 를 사용하지만
                // JQcloud 의 경우 text 와 weight 를 사용한다.

                // 이후 다시 array 에 담기
                informationObject.put("text", list);
                informationObject.put("weight", result.get(list));

                if(index < 30){
                    komoranMapList.add(informationObject);
                    index++;
                } else {
                    break;
                }
            }

            return komoranMapList;
        } catch (Exception e) {
            List<Map<String, Object>> errorMapList = new ArrayList<>();
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("errorMsg", e.getMessage());
            errorMap.put("exceptionObj", e);
            errorMapList.add(errorMap);
            return errorMapList;
        }
    }

    public static LinkedHashMap<String, Integer> sortMapByValue(Map<String, Integer> map) {
        List<Map.Entry<String, Integer>> entries = new LinkedList<>(map.entrySet());
        Collections.sort(entries, Map.Entry.comparingByValue(Collections.reverseOrder()));

        LinkedHashMap<String, Integer> result = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : entries) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }
}
