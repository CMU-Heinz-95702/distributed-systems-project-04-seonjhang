/**
 * Author: Seon Jhang
 * AndrewID: sjhang
 */

import java.io.*;
import java.net.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.WebServlet;
import org.json.*;

@WebServlet("/trivia")
public class TriviaServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse response) throws ServletException, IOException {
        try {
            // 1. Fetch trivia data from Trivia API
            URL url = new URL("https://opentdb.com/api.php?amount=1&type=multiple");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder resultBuilder = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                resultBuilder.append(inputLine);
            }
            in.close();

            String json = resultBuilder.toString();

            // 2. Parse
            JSONObject jsonObject = new JSONObject(json);
            JSONArray results = jsonObject.getJSONArray("results");
            if (results.length() == 0) {
                sendError(response, "No trivia data available.");
                return;
            }

            JSONObject trivia = results.getJSONObject(0);
            String question = trivia.getString("question");
            String correctAnswer = trivia.getString("correct_answer");
            JSONArray incorrects = trivia.getJSONArray("incorrect_answers");

            // 3. Combine answers and shuffle
            List<String> allOptions = new ArrayList<>();
            allOptions.add(correctAnswer);
            for (int i = 0; i < incorrects.length(); i++) {
                allOptions.add(incorrects.getString(i));
            }
            Collections.shuffle(allOptions);
            int correctIndex = allOptions.indexOf(correctAnswer);

            // 4. Build response JSON
            JSONObject responseJson = new JSONObject();
            responseJson.put("question", question);
            responseJson.put("correct_answer", correctAnswer);
            responseJson.put("correct_index", correctIndex);

            // 5. Send response
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().print(responseJson.toString());
        } catch (Exception e) {
            e.printStackTrace();
            sendError(response, "Internal server error: " + e.getMessage());
        }
    }

    private void sendError(HttpServletResponse response, String errorMessage) throws IOException {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        JSONObject errorJson = new JSONObject();
        errorJson.put("error", errorMessage);
        response.getWriter().print(errorJson.toString());
    }
}
