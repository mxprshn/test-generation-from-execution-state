package demo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CounterScenarioTest {
    @Test
    void scenario() {
        Counter c = new Counter();
        assertEquals(5, c.add("hello"));
        assertEquals(10, c.add("world"));
        assertEquals(-1, c.safeAdd(null));
        assertEquals(2, c.seen().size());
    }
}
