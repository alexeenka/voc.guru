package guru.h4t_eng.datasource;

import guru.h4t_eng.rest.WordListDto;
import org.json.JSONObject;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;

/**
 * LoadWordsMethodReturn.
 *
 * Created by Alexey Alexeenka on 24.09.2015.
 */
public class LoadWordsMethodReturn {

    private final ArrayList<WordListDto> wordList;
    private final String pagingState;

    public LoadWordsMethodReturn(ArrayList<WordListDto> wordList, String pagingState) {
        this.wordList = wordList;
        this.pagingState = pagingState;
    }

    public ArrayList<WordListDto> getWordList() {
        return wordList;
    }

    public String getPagingState() {
        return pagingState;
    }

    public Response response() {
        JSONObject result = new JSONObject();
        result.put("wordList", wordList);
        result.put("pagingState", pagingState);

        return Response.ok(result.toString(), MediaType.APPLICATION_JSON_TYPE).build();
    }

}
