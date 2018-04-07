package guru.h4t_eng.rest.json;

/**
 * JsonDto.
 *
 * Created by aalexeenka on 18.05.2016.
 */
public interface JsonDto {

    String toJson();

    default String toJsonArray(JsonDto[] dtos) {

        FastJsonBuilder builder = new FastJsonBuilder();
        for (JsonDto dto : dtos) {
            builder.append(dto.toJson());
        }
        return builder.finish();
    }

}
