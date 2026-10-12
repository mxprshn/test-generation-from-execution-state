package io.aiven.klaw.helpers;

import org.junit.jupiter.api.Test;
import java.util.List;
import org.mockito.Mockito;
import java.util.function.BiFunction;
import org.mockito.invocation.InvocationOnMock;
import io.aiven.klaw.helpers.Pager;
import org.junit.jupiter.api.Assertions;
import java.util.ArrayList;
import java.lang.*;
import static io.aiven.klaw.helpers.Pager.PageContext;

class PagerReproducedTests {

    @Test
    void getItemsListReproducedTest() {
        BiFunction<Object, Object, Object> mocked = Mockito.mock(BiFunction.class);
        Mockito.when(mocked.apply(Mockito.<Object>any(), Mockito.<Object>any())).thenReturn("1").thenReturn("2").thenReturn("3").thenReturn("4").thenReturn("5");
        List<Object> v = Pager.getItemsList("1", "2", 5, (List<Object>) (Object) List.of("1", "2", "3", "4", "5", "6", "7"), (BiFunction<PageContext, Object, Object>) (Object) mocked);
        Assertions.assertEquals(5, v.size());
    }

    @Test
    void getItemsListReproducedTest1() {
        BiFunction<Object, Object, Object> mocked = Mockito.mock(BiFunction.class);
        Mockito.when(mocked.apply(Mockito.<Object>any(), Mockito.<Object>any())).thenReturn("1").thenReturn("2").thenReturn("3").thenReturn("4").thenReturn("5");
        List<Object> v = Pager.getItemsList("1", "1", 5, (List<Object>) (Object) List.of("1", "2", "3", "4", "5", "6", "7"), (BiFunction<PageContext, Object, Object>) (Object) mocked);
        Assertions.assertEquals(5, v.size());
    }

    @Test
    void getItemsListReproducedTest2() {
        BiFunction<Object, Object, Object> mocked = Mockito.mock(BiFunction.class);
        Mockito.when(mocked.apply(Mockito.<Object>any(), Mockito.<Object>any())).thenReturn("1").thenReturn("2").thenReturn("3").thenReturn("4").thenReturn("5");
        List<Object> v = Pager.getItemsList("1", "", 5, (List<Object>) (Object) List.of("1", "2", "3", "4", "5", "6", "7"), (BiFunction<PageContext, Object, Object>) (Object) mocked);
        Assertions.assertEquals(5, v.size());
    }

    @Test
    void getItemsListReproducedTest3() {
        ArrayList<Object> v = new ArrayList<Object>();
        v.add("1");
        v.add("2");
        v.add("3");
        v.add("4");
        v.add("5");
        v.add("6");
        v.add("7");
        v.add("8");
        v.add("9");
        v.add("10");
        v.add("11");
        v.add("12");
        BiFunction<Object, Object, Object> mocked = Mockito.mock(BiFunction.class);
        Mockito.when(mocked.apply(Mockito.<Object>any(), Mockito.<Object>any())).thenReturn("1").thenReturn("2").thenReturn("3").thenReturn("4").thenReturn("5");
        List<Object> v1 = Pager.getItemsList("1", "2", 5, (List<Object>) (Object) v, (BiFunction<PageContext, Object, Object>) (Object) mocked);
        Assertions.assertEquals(5, v1.size());
    }
}
