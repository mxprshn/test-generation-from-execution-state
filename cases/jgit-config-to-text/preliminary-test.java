package org.eclipse.jgit.lib;

import org.junit.jupiter.api.Test;
import java.util.concurrent.atomic.AtomicReference;
import org.mockito.Mockito;
import org.eclipse.jgit.lib.ConfigSnapshot;
import org.springframework.test.util.ReflectionTestUtils;
import org.eclipse.jgit.lib.DefaultTypedConfigGetter;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.events.ListenerList;
import java.lang.*;

class ConfigReproducedTests {

    @Test
    void toTextReproducedTest() {
        AtomicReference<Object> v = new AtomicReference<Object>();
        ReflectionTestUtils.setField(v, "value", Mockito.mock(ConfigSnapshot.class));
        ReflectionTestUtils.setField(Config.class, "typedGetter", new DefaultTypedConfigGetter());
        Config v1 = new Config(null);
        ReflectionTestUtils.setField(v1, "listeners", Mockito.mock(ListenerList.class));
        ReflectionTestUtils.setField(v1, "state", v);
        ReflectionTestUtils.setField(v1, "baseConfig", null);
        v1.toText();
    }

}
