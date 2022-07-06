package pl.andrzejo.aspm.error;

import org.junit.jupiter.api.Test;
import pl.andrzejo.aspm.factory.BeanFactory;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class DefaultErrorHandlerTest {

    @Test
    void shouldHandleDefaultError() {
        //given
        UnhandledExceptionDialog mock = mock(UnhandledExceptionDialog.class);
        BeanFactory.overrideInstance(UnhandledExceptionDialog.class, mock);
        DefaultErrorHandler handler = new DefaultErrorHandler();

        //when
        handler.uncaughtException(mock(Thread.class), new Throwable("Some message"));

        //then
        verify(mock).confirmExit(anyObject(), anyString());
    }
}