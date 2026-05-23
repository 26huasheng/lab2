package spellcheck;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class LanguageToolAdapter implements ISpellChecker {
    private HttpClient client;

    public LanguageToolAdapter() {
        // 初始化 HTTP 客户端
        this.client = HttpClient.newHttpClient();
    }

    @Override
    public List<SpellError> check(String text) {
        List<SpellError> results = new ArrayList<>();
        if (text == null || text.trim().isEmpty()) {
            return results;
        }

        try {
            System.out.println("[系统提示] 正在调用 LanguageTool 云端 API 进行检查...");

            // 构造 POST 请求参数
            String body = "language=en-US&text=" + java.net.URLEncoder.encode(text, "UTF-8");

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.languagetool.org/v2/check"))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            // 发送请求
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // 使用第三方库 Gson 解析返回的 JSON 数据
            JsonObject jsonObject = JsonParser.parseString(response.body()).getAsJsonObject();
            JsonArray matches = jsonObject.getAsJsonArray("matches");

            for (JsonElement matchElement : matches) {
                JsonObject match = matchElement.getAsJsonObject();
                int offset = match.get("offset").getAsInt();
                int length = match.get("length").getAsInt();

                // 根据偏移量截取错误的单词
                String badWord = text.substring(offset, offset + length);

                // 提取修改建议
                JsonArray replacements = match.getAsJsonArray("replacements");
                String suggestion = replacements.size() > 0 ?
                                    replacements.get(0).getAsJsonObject().get("value").getAsString() : "无建议";

                results.add(new SpellError(badWord, suggestion));
            }
        } catch (Exception e) {
            System.err.println("[Error] 云端拼写检查失败，请检查网络或稍后重试: " + e.getMessage());
        }
        return results;
    }
}
