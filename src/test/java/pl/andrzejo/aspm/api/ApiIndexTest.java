package pl.andrzejo.aspm.api;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static pl.andrzejo.aspm.api.SimpleHttpServer.Method.Post;

class ApiIndexTest {

    @Test
    void shouldGetApiIndex() {
        //given
        ApiIndex index = new ApiIndex();
        List<RestApiService.Endpoint> endpoints = Collections.singletonList(new RestApiService.Endpoint(Post, "/api/endpoint", "Some endpoint."));

        //when
        String html = index.getHtml(endpoints);

        //then
        assertThat(html)
                .contains("<div class=\"item\">")
                .contains("<div class=\"method post\"><span>POST</span></div>")
                .contains("<div class=\"path\">/api/endpoint</div>")
                .contains("<div class=\"desc\">Some endpoint.</div>")
                .contains("<div class=\"curl\"><span>curl -X POST http://localhost:4255/api/endpoint -d 'BODY' </span></div>");
    }

}