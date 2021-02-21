package ru.obukhov.investor.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import okhttp3.HttpUrl;
import org.springframework.validation.annotation.Validated;
import ru.obukhov.investor.util.CollectionsUtils;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Class, containing URL segments and query limit for such URL
 */
@Data
@Validated
@NoArgsConstructor
@AllArgsConstructor
public class UrlLimit {

    private List<String> segments;

    @NotNull
    private Integer limit;

    public boolean matchesUrl(HttpUrl url) {
        return CollectionsUtils.containsList(url.pathSegments(), segments);
    }

}