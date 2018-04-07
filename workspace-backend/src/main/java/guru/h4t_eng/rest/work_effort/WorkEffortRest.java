package guru.h4t_eng.rest.work_effort;

import guru.h4t_eng.datasource.WorkEffortDataSource;
import guru.h4t_eng.logs.AppLoggerFactory;
import guru.h4t_eng.rest.Main4Rest;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 * WorkEffortRest.
 *
 * Created by aalexeenka on 3/21/2016.
 */
@Path("/work-effort")
public class WorkEffortRest extends Main4Rest {

    private static final Logger LOG = AppLoggerFactory.getH4TLog(WorkEffortRest.class);

    private WorkEffortDataSource workEffortDataSource = WorkEffortDataSource.getInstance();

    @Path("/time-spent-for-day")
    @POST
    public Response getTimeSpent(@Context HttpServletRequest httpRequest) {
        try {
            String jsonStr = IOUtils.toString(httpRequest.getInputStream(), "UTF-8");
            JSONObject jsonObj = new JSONObject(jsonStr);

            Integer year = jsonObj.getInt("year");
            Integer dayOfYear = jsonObj.getInt("dayOfYear");
            Long spentTime = workEffortDataSource.getTime(getUserId(httpRequest), year, dayOfYear);

            JSONObject result = new JSONObject();
            result.put("spentTime", spentTime);

            return Response.ok(result.toString(), MediaType.APPLICATION_JSON_TYPE).build();


        } catch (IOException | JSONException e)
        {
            LOG.error("Can't get json string", e);
            return Response.status(Response.Status.FORBIDDEN).build();
        }
    }

    @Path("/training-calendar")
    @POST
    public Response trainingCalendar(@Context HttpServletRequest httpRequest) {
        try {
            String jsonStr = IOUtils.toString(httpRequest.getInputStream(), "UTF-8");
            JSONObject jsonObj = new JSONObject(jsonStr);

            Integer year = jsonObj.getInt("year");
            Integer startDay = jsonObj.getInt("startDay");
            Integer count = jsonObj.getInt("count");

            final Long[] trainingCalendar = workEffortDataSource.getTrainingCalendar(getUserId(httpRequest), year, startDay, count);

            JSONObject result = new JSONObject();
            result.put("trainingCalendar", trainingCalendar);

            return Response.ok(result.toString(), MediaType.APPLICATION_JSON_TYPE).build();


        } catch (IOException | JSONException e)
        {
            LOG.error("Can't get json string", e);
            return Response.status(Response.Status.FORBIDDEN).build();
        }
    }

}
