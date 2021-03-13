package ru.obukhov.trader.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import okhttp3.HttpUrl;
import org.springframework.validation.annotation.Validated;
import ru.obukhov.trader.common.util.CollectionsUtils;

import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Class, containing URL segments and query limit for such URL
 */
@Data
@Validated
@NoArgsConstructor
@AllArgsConstructor
public class UrlLimit {

    @Size(min = 1, message = "segments must not be empty")
    private List<String> segments;

    @Min(value = 1L, message = "limit must be positive")
    private int limit;

    /**
     * @return true, if given {@code url} contains all segments of current {@code UrlLimit} in same order,
     * otherwise false
     */
    public boolean matchesUrl(HttpUrl url) {
        return CollectionsUtils.containsList(url.pathSegments(), segments);
    }

    /**
     * @return fragment of URL, contained in current {@code UrlLimit}
     */
    public String getUrl() {
        return segments.stream().collect(Collectors.joining("/", "/", ""));
    }

}