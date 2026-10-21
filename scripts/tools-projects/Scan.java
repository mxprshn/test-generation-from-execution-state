import com.sun.jdi.*;
import com.sun.jdi.connect.*;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;
import java.nio.file.*;
import java.util.*;

/**
 * Scenario search: java Scan PORT OUT Class#method[,Class#method...]
 * Counts entries into target methods per test method (outermost frame of a class named *Test/*Tests/*IT).
 */
public class Scan {
    public static void main(String[] a) throws Exception {
        int port = Integer.parseInt(a[0]);
        Path out = Paths.get(a[1]);
        Map<String, Set<String>> targets = new HashMap<>();
        for (String t : a[2].split(",")) {
            String[] p = t.split("#");
            targets.computeIfAbsent(p[0], k -> new HashSet<>()).add(p[1]);
        }
        T = targets;
        ListeningConnector c = (ListeningConnector) Bootstrap.virtualMachineManager().allConnectors().stream()
                .filter(x -> x.name().equals("com.sun.jdi.SocketListen")).findFirst().get();
        Map<String, Connector.Argument> args = c.defaultArguments();
        args.get("port").setValue(String.valueOf(port));
        args.get("localAddress").setValue("127.0.0.1");
        args.get("timeout").setValue("900000");
        c.startListening(args);
        VirtualMachine vm = c.accept(args);
        c.stopListening(args);
        EventRequestManager erm = vm.eventRequestManager();
        for (String cls : targets.keySet()) {
            ClassPrepareRequest r = erm.createClassPrepareRequest();
            r.addClassFilter(cls);
            r.enable();
            for (ReferenceType rt : vm.classesByName(cls)) watch(erm, rt);
        }
        Map<String, Integer> counts = new TreeMap<>();
        vm.resume();
        EventQueue q = vm.eventQueue();
        boolean done = false;
        while (!done) {
            EventSet set;
            try { set = q.remove(); } catch (VMDisconnectedException e) { break; }
            for (Event e : set) {
                if (e instanceof ClassPrepareEvent cp) watch(erm, cp.referenceType());
                else if (e instanceof BreakpointEvent me) {
                    Method m = me.location().method();
                    int tot = total.merge(m, 1, Integer::sum);
                    if (tot >= CAP) { me.request().disable(); capped.add(m.declaringType().name() + "#" + m.name()); }
                    Set<String> names = targets.get(m.declaringType().name());
                    if (names == null || !names.contains(m.name()) || m.isBridge() || m.isSynthetic()) continue;
                    String test = "?";
                    try {
                        for (StackFrame f : me.thread().frames()) {
                            String cn = f.location().declaringType().name();
                            String sn = cn.replaceAll(".*\\.", "").replaceAll("\\$.*", "");
                            if (sn.endsWith("Test") || sn.endsWith("Tests") || sn.endsWith("IT")) {
                                test = cn.replaceAll("\\$.*", "") + "#" + f.location().method().name();
                            }
                        }
                    } catch (Exception ex) { test = "ERR"; }
                    String key = m.declaringType().name() + "#" + m.name() + "(" + String.join(",", m.argumentTypeNames()) + ")"
                            + (m.isStatic() ? " static" : "") + (m.isPublic() ? "" : " nonpublic") + " <- " + test;
                    counts.merge(key, 1, Integer::sum);
                } else if (e instanceof VMDeathEvent || e instanceof VMDisconnectEvent) done = true;
            }
            if (!done) try { set.resume(); } catch (VMDisconnectedException ex) { done = true; }
        }
        StringBuilder b = new StringBuilder();
        counts.forEach((k, v) -> b.append(v).append('\t').append(k).append('\n'));
        for (String cc : capped) b.append("CAPPED\t").append(cc).append('\n');
        Files.writeString(out, b.toString());
    }

    static Map<String, Set<String>> T;
    static final int CAP = 300;
    static Map<Method, Integer> total = new HashMap<>();
    static Set<String> capped = new TreeSet<>();
    static void watch(EventRequestManager erm, ReferenceType rt) {
        Set<String> names = T.get(rt.name());
        if (names == null) return;
        for (Method m : rt.methods()) {
            if (!names.contains(m.name()) || m.isAbstract() || m.isNative() || m.location() == null) continue;
            BreakpointRequest b = erm.createBreakpointRequest(m.location());
            b.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
            b.enable();
        }
    }
}
