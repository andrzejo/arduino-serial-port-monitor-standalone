package pl.andrzejo.aspm.factory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BeanFactoryTest {

    @BeforeEach
    void setUp() {
        BeanFactory.reset();
    }

    @Test
    void shouldCreateInstanceByDefaultConstructor() {
        //given
        Class<SomeBean> type = SomeBean.class;

        //when
        SomeBean instance = BeanFactory.instance(type);

        //then
        assertEquals("msg", instance.msg());
        assertEquals(1, instance.instanceCount());
    }


    @Test
    void shouldReturnCreatedInstance() {
        //given
        Class<SomeBean> type = SomeBean.class;
        BeanFactory.instance(type);
        BeanFactory.instance(type);

        //when
        SomeBean instance = BeanFactory.instance(type);

        //then
        assertEquals("msg", instance.msg());
        assertEquals(1, instance.instanceCount());
    }

    @Test
    void shouldOverrideInstance() {
        //given
        Class<SomeBean> type = SomeBean.class;
        SomeBean bean1 = BeanFactory.instance(type);

        //when
        BeanFactory.overrideInstance(type, new SomeBean("Overridden bean"));
        SomeBean overriddenBean = BeanFactory.instance(type);

        //then
        assertEquals("msg", bean1.msg());

        assertEquals("Overridden bean", overriddenBean.msg());
        assertEquals(2, overriddenBean.instanceCount());
    }

    @Test
    void shouldCreateInstanceByFactoryMethod() {
        //given
        Class<SomeBean> type = SomeBean.class;

        //when
        SomeBean bean = BeanFactory.instance(type, () -> new SomeBean("from factory"));

        //then
        assertEquals("from factory", bean.msg());
    }


    static class SomeBean {
        private static int instCounter = 0;
        private final String msg;

        public SomeBean() {
            this("msg");
        }

        public SomeBean(String msg) {
            this.msg = msg;
            instCounter++;
        }

        public String msg() {
            return msg;
        }

        public int instanceCount() {
            return instCounter;
        }
    }
}