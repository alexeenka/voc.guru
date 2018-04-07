package guru.h4t_eng.rest.words;

import guru.h4t_eng.datasource.CounterDataSource;
import guru.h4t_eng.datasource.TrainingDataSource;
import guru.h4t_eng.datasource.WorkoutType;
import guru.h4t_eng.datasource.training.Training;
import guru.h4t_eng.rest.Main4Rest;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.UUID;

/**
 * CounterRest.
 *
 * Created by aalexeenka on 1/20/2016.
 */
@Path("/counter")
public class CounterRest extends Main4Rest {

    private static CounterDataSource counterDataSource = CounterDataSource.getInstance();
    private static TrainingDataSource trainingDataSource = TrainingDataSource.getInstance();

    @Path("/count-words-and-indicators")
    @GET
    public Response countWords(@Context HttpServletRequest httpRequest) {
        StringBuilder result = new StringBuilder();
        result.append("{");
        UUID userId = getUserId(httpRequest);
        result.append("\"all\":").append(counterDataSource.countWord(userId));

        final Training training = trainingDataSource.getTraining(userId);
        for (WorkoutType workoutType : WorkoutType.values()) {
            final int count = training.getCountRemainedTrainingWords(workoutType);
            result.append(", \"").append(workoutType.columnName).append("\":").append(count);
        }

        result.append("}");
        return Response.ok(result.toString(), MediaType.APPLICATION_JSON_TYPE).build();
    }

    @Path("/count-user-words")
    @GET
    public Response countUserWords(@Context HttpServletRequest httpRequest) {
        final Long count = counterDataSource.countWord(getUserId(httpRequest));
        return Response.ok(count.toString(), MediaType.APPLICATION_JSON_TYPE).build();
    }

    @Path("/count-remained-def-eng-words")
    @GET
    public Response countRemainedDefEngWords(@Context HttpServletRequest httpRequest) {
        final int count = trainingDataSource.countRemainedTrainingWords(getUserId(httpRequest), WorkoutType.DEF_ENG);
        return Response.ok(Integer.toString(count), MediaType.APPLICATION_JSON_TYPE).build();
    }

    @Path("/count-remained-eng-rus-words")
    @GET
    public Response countRemainedEngRusWords(@Context HttpServletRequest httpRequest) {
        final int count = trainingDataSource.countRemainedTrainingWords(getUserId(httpRequest), WorkoutType.ENG_RUS);
        return Response.ok(Integer.toString(count), MediaType.APPLICATION_JSON_TYPE).build();
    }

    @Path("/count-remained-rus-eng-words")
    @GET
    public Response countRemainedRusEngWords(@Context HttpServletRequest httpRequest) {
        final int count = trainingDataSource.countRemainedTrainingWords(getUserId(httpRequest), WorkoutType.RUS_ENG);
        return Response.ok(Integer.toString(count), MediaType.APPLICATION_JSON_TYPE).build();
    }

    @Path("/count-remained-img-eng-words")
    @GET
    public Response countRemainedImgEngWords(@Context HttpServletRequest httpRequest) {
        final int count = trainingDataSource.countRemainedTrainingWords(getUserId(httpRequest), WorkoutType.IMG_ENG);
        return Response.ok(Integer.toString(count), MediaType.APPLICATION_JSON_TYPE).build();
    }

    @Path("/count-remained-sen-eng-words")
    @GET
    public Response countRemainedSenEngWords(@Context HttpServletRequest httpRequest) {
        final int count = trainingDataSource.countRemainedTrainingWords(getUserId(httpRequest), WorkoutType.SEN_ENG);
        return Response.ok(Integer.toString(count), MediaType.APPLICATION_JSON_TYPE).build();
    }

}
