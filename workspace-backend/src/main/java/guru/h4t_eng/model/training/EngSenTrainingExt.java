package guru.h4t_eng.model.training;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Extension for training SEN-ENG.
 *
 * <p>
 * Created by aalexeenka on 29.09.2016.
 */
public class EngSenTrainingExt {

    /**
     * User see sentence question, sQ
     */
    private String sQ;

    /**
     * User hear sentence to hear, sH
     */
    private String sH;

    /**
     * Sentence Answer
     */
    private String sA;

    /**
     * User need to input following answer
     */
    private String answer;

    public EngSenTrainingExt(String sentenceAnswer, String sentenceQuestion, String sentenceHear, String answer) {
        this.sA = sentenceAnswer;
        this.sQ = sentenceQuestion;
        this.sH = sentenceHear;
        this.answer = answer;
    }

    public String getsQ() {
        return sQ;
    }

    public void setsQ(String sQ) {
        this.sQ = sQ;
    }

    public String getsH() {
        return sH;
    }

    public void setsH(String sH) {
        this.sH = sH;
    }

    public String getsA() {
        return sA;
    }

    public void setsA(String sA) {
        this.sA = sA;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("sA", sA)
                .append("sQ", sQ)
                .append("sH", sH)
                .append("answer", answer)
                .toString();
    }
}
