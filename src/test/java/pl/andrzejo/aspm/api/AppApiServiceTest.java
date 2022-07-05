package pl.andrzejo.aspm.api;

import org.junit.jupiter.api.Test;
import pl.andrzejo.aspm.factory.ObjectFactory;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import static pl.andrzejo.aspm.api.SimpleHttpServer.Method.Get;
import static pl.andrzejo.aspm.api.SimpleHttpServer.Method.Post;

class AppApiServiceTest {

    @Test
    void shouldAddAllEndpoints() {
        //given
        AppApiService service = new AppApiService();
        SimpleHttpServer server = mock(SimpleHttpServer.class);
        ObjectFactory.overrideInstance(SimpleHttpServer.class, server);

        //when
        service.start();

        //then
        verify(server).addEndpoint(eq(Post), eq("/api/open"), anyObject());
        verify(server).addEndpoint(eq(Post), eq("/api/close"), anyObject());
        verify(server).addEndpoint(eq(Get), eq("/api/status"), anyObject());
        verify(server).addEndpoint(eq(Get), eq("/api/devices"), anyObject());
    }
}