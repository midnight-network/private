package net.midnightmc.proxy.discord;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class CovidNotify {

    public void get() {
        Document respond;
        try {
            String today = LocalDate.now(ZoneId.of("Asia/Seoul")).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            /*URL*/
            String urlBuilder = "https://openapi.data.go.kr/openapi/service/rest/Covid19/getCovid19InfStateJson?"
                    + URLEncoder.encode("serviceKey", StandardCharsets.UTF_8)
                    + URLEncoder.encode("NIfHbuKlPxJ0j7pW6Ckpf7g18Rna1ybIpk%2B2v0xLuiHHicEm%2FAFDK1KhsQR1zHMyMB5pFYvpRk2KsLQJRkCPHA%3D%3D", StandardCharsets.UTF_8) /*Service Key*/
                    + "&" + URLEncoder.encode("pageNo", StandardCharsets.UTF_8)
                    + "=" + URLEncoder.encode("1", StandardCharsets.UTF_8)  /*페이지번호*/
                    + "&" + URLEncoder.encode("numOfRows", StandardCharsets.UTF_8)
                    + "=" + URLEncoder.encode("10", StandardCharsets.UTF_8)  /*한 페이지 결과 수*/
                    + "&" + URLEncoder.encode("startCreateDt", StandardCharsets.UTF_8)
                    + "=" + URLEncoder.encode(today, StandardCharsets.UTF_8)  /*검색할 생성일 범위의 시작*/
                    + "&" + URLEncoder.encode("endCreateDt", StandardCharsets.UTF_8)
                    + "=" + URLEncoder.encode(today, StandardCharsets.UTF_8); /*검색할 생성일 범위의 종료*/
            URL url = new URL(urlBuilder);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-type", "application/json");
            System.out.println("Response code: " + conn.getResponseCode());
            respond = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(conn.getInputStream());
        } catch (IOException | ParserConfigurationException | SAXException ex) {
            ex.printStackTrace();
            return;
        }
        NodeList list = respond.getDocumentElement().getElementsByTagName("items");
    }

}
