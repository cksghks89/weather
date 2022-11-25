package com.example.weather.service;

import com.example.weather.domain.DateWeather;
import com.example.weather.domain.Diary;
import com.example.weather.repository.DateWeatherRepository;
import com.example.weather.repository.DiaryRepository;
import lombok.RequiredArgsConstructor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class DiaryService {
    private final DiaryRepository diaryRepository;
    private final DateWeatherRepository dateWeatherRepository;

    @Value("${openweathermap.key}")
    private String apiKey;

    @Scheduled(cron = "0 0 1 * * *")
    public void saveWeatherDate() {
        dateWeatherRepository.save(getWeatherFromApi());
    }

    private DateWeather getWeatherFromApi() {
        // open weather map 에서 날씨 데이터 가져오기
        String weatherData = getWeatherString();

        // 받아온 날씨 json 파싱하기
        Map<String, Object> parseWeather = parseWeather(weatherData);

        return DateWeather.builder()
                .date(LocalDate.now())
                .weather(parseWeather.get("main").toString())
                .icon(parseWeather.get("icon").toString())
                .temperature((Double) parseWeather.get("temp"))
                .build();
    }

    public void createDiary(LocalDate date, String text) {
        // 날씨 데이터 가져오기 (API or DB 에서 가져오기)
        DateWeather dateWeather = getDateWeather(date);

        // 파싱된 데이터 + 일기 값 DB에 넣기
        Diary newDiary = new Diary();
        newDiary.setDateWeather(dateWeather);

        diaryRepository.save(newDiary);
    }

    private DateWeather getDateWeather(LocalDate date) {
        List<DateWeather> dateWeatherListFromDB = dateWeatherRepository.findAllByDate(date);
        if(dateWeatherListFromDB.size() == 0){
            // 새로 API 에서 날씨 정보를 가져옴
            // 정책상.. 현재 날씨를 가져오도록 하거나 날씨없이 일기를 쓰도록
            return getWeatherFromApi();
        }else{
            return dateWeatherListFromDB.get(0);
        }
    }

    private String getWeatherString() {
        String apiUrl = "https://api.openweathermap.org/data/2.5/weather?q=seoul&appid=" + apiKey;

        try {
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();

            BufferedReader br;
            if (responseCode == 200) {
                br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            } else {
                br = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            }

            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = br.readLine()) != null) {
                response.append(inputLine);
            }
            br.close();

            return response.toString();
        } catch (Exception e) {
            return "failed to get API response";
        }
    }

    private Map<String, Object> parseWeather(String jsonString) {
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject;

        try {
            jsonObject = (JSONObject) jsonParser.parse(jsonString);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        Map<String, Object> resultMap = new HashMap<>();

        JSONObject mainData = (JSONObject) jsonObject.get("main");
        resultMap.put("temp", mainData.get("temp"));

        JSONArray weatherDataArray = (JSONArray) jsonObject.get("weather");
        JSONObject weatherData = (JSONObject) weatherDataArray.get(0);

        resultMap.put("main", weatherData.get("main"));
        resultMap.put("icon", weatherData.get("icon"));

        return resultMap;
    }

    @Transactional(readOnly = true)
    public List<Diary> readDiary(LocalDate date) {
        return diaryRepository.findAllByDate(date);
    }

    @Transactional(readOnly = true)
    public List<Diary> readDiaries(LocalDate startDate, LocalDate endDate) {
        return diaryRepository.findAllByDateBetween(startDate, endDate);
    }

    public void updateDiary(LocalDate date, String text) {
        Diary nowDiary = diaryRepository.getFirstByDate(date);
        nowDiary.setText(text);

        diaryRepository.save(nowDiary);
    }

    public void deleteDiary(LocalDate date) {
        diaryRepository.deleteAllByDate(date);
    }
}
