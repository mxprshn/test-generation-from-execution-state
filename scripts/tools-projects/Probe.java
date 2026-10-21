import com.sun.jdi.*;
import com.sun.jdi.connect.*;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;
import java.nio.file.*;
import java.util.*;

/**
 * Debugger check of one case: java Probe PORT Class METHOD [argTypesCsv|-] OUT.json
 * Listens (the debuggee connects with server=n), stops at entry and exit of the target method,
 * reads args, fields of this, return value. Writes a JSON summary.
 */
public class Probe {
    static String cls, name, sig;
    static int entries, exits, exceptionExits, argsOk, thisOk, returnOk, statics;
    static List<String> samples = new ArrayList<>();
    static String error = null;

    public static void main(String[] a) throws Exception {
        int port = Integer.parseInt(a[0]);
        cls = a[1]; name = a[2]; sig = a[3].equals("-") ? null : a[3];
        Path out = Paths.get(a[4]);
        long t0 = System.currentTimeMillis();
        ListeningConnector c = (ListeningConnector) Bootstrap.virtualMachineManager().allConnectors().stream()
                .filter(x -> x.name().equals("com.sun.jdi.SocketListen")).findFirst().get();
        Map<String, Connector.Argument> args = c.defaultArguments();
        args.get("port").setValue(String.valueOf(port));
        args.get("localAddress").setValue("127.0.0.1");
        args.get("timeout").setValue("900000");
        c.startListening(args);
        VirtualMachine vm = c.accept(args);
        c.stopListening(args);
        long attached = System.currentTimeMillis();
        EventRequestManager erm = vm.eventRequestManager();
        ERM = erm;
        ClassPrepareRequest cpr = erm.createClassPrepareRequest();
        cpr.addClassFilter(cls);
        cpr.enable();
        for (ReferenceType rt : vm.classesByName(cls)) watch(erm, rt);
        vm.resume();
        EventQueue q = vm.eventQueue();
        boolean done = false;
        while (!done) {
            EventSet set = q.remove();
            for (Event e : set) {
                try {
                    if (e instanceof ClassPrepareEvent cp) watch(erm, cp.referenceType());
                    else if (e instanceof BreakpointEvent be) onEntry(be);
                    else if (e instanceof MethodExitEvent mx && match(mx.method())) onExit(mx);
                    else if (e instanceof ExceptionEvent xe) onException(xe);
                    else if (e instanceof VMDeathEvent || e instanceof VMDisconnectEvent) done = true;
                } catch (Exception ex) {
                    if (error == null) error = ex.toString();
                }
            }
            if (!done) try { set.resume(); } catch (VMDisconnectedException ex) { done = true; }
        }
        long end = System.currentTimeMillis();
        StringBuilder j = new StringBuilder("{");
        j.append("\"target\":\"").append(esc(cls + "#" + name + (sig == null ? "" : "(" + sig + ")"))).append("\",");
        j.append("\"hits_entry\":").append(entries).append(",\"hits_exit\":").append(exits)
                .append(",\"exception_exits\":").append(entries - exits)
                .append(",\"static\":").append(statics > 0)
                .append(",\"args_ok\":").append(entries > 0 && argsOk == entries)
                .append(",\"this_ok\":").append(entries > 0 && (thisOk == entries || statics > 0))
                .append(",\"return_ok\":").append(exits > 0 && returnOk == exits).append(",\"last_exception\":").append(lastException == null ? "null" : "\"" + lastException + "\"")
                .append(",\"wait_attach_s\":").append((attached - t0) / 1000.0)
                .append(",\"debug_s\":").append((end - attached) / 1000.0)
                .append(",\"error\":").append(error == null ? "null" : "\"" + esc(error) + "\"")
                .append(",\"samples\":[");
        for (int i = 0; i < samples.size(); i++) j.append(i > 0 ? "," : "").append("\"").append(esc(samples.get(i))).append("\"");
        j.append("]}");
        Files.writeString(out, j.toString());
        System.out.println(j);
    }

    static boolean match(Method m) {
        if (!m.name().equals(name) || m.isBridge() || m.isSynthetic()) return false;
        if (!m.declaringType().name().equals(cls)) return false;
        if (sig == null) return true;
        return String.join(",", m.argumentTypeNames()).equals(sig);
    }

    static EventRequestManager ERM;
    static Map<Long, MethodExitRequest> exitReqs = new HashMap<>();
    static Map<Long, Integer> depth = new HashMap<>();
    static Map<Long, ExceptionRequest> excReqs = new HashMap<>();
    static Map<Long, Integer> inTarget = new HashMap<>();
    static void watch(EventRequestManager erm, ReferenceType rt) {
        if (!rt.name().equals(cls)) return;
        for (Method m : rt.methods()) {
            if (!match(m) || m.isAbstract() || m.isNative() || m.location() == null) continue;
            BreakpointRequest b = erm.createBreakpointRequest(m.location());
            b.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
            b.enable();
        }
    }

    static void onEntry(BreakpointEvent me) throws Exception {
        entries++;
        ThreadReference th = me.thread();
        StackFrame f = th.frame(0);
        Method m = me.location().method();
        // exit of this invocation: MethodExit filtered to the thread, matched by frame depth
        long tid = th.uniqueID();
        if (!exitReqs.containsKey(tid)) {
            MethodExitRequest ex = ERM.createMethodExitRequest();
            ex.addThreadFilter(th);
            ex.addClassFilter(m.declaringType());
            ex.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
            ex.enable();
            exitReqs.put(tid, ex);
            ExceptionRequest xr = ERM.createExceptionRequest(null, true, true);
            xr.addThreadFilter(th);
            xr.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
            excReqs.put(tid, xr);
        }
        // exit and exception events are requested only while the target runs on this thread:
        // method-exit requests force the interpreter, so they must not stay enabled
        inTarget.merge(tid, 1, Integer::sum);
        excReqs.get(tid).enable();
        exitReqs.get(tid).enable();
        if (m.isStatic()) statics++;
        StringBuilder s = new StringBuilder("entry#" + entries + " ");
        try {
            List<Value> av = f.getArgumentValues();
            s.append("args=").append(render(av));
            argsOk++;
        } catch (Exception ex) { s.append("args=ERR ").append(ex); }
        if (!m.isStatic()) {
            ObjectReference self = f.thisObject();
            if (self != null) {
                int n = 0; StringBuilder fs = new StringBuilder();
                for (Field fl : self.referenceType().allFields()) {
                    if (fl.isStatic()) continue;
                    if (n < 4) fs.append(fl.name()).append('=').append(short1(self.getValue(fl))).append(' ');
                    n++;
                }
                s.append(" this{").append(n).append(" fields: ").append(fs).append('}');
                thisOk++;
            }
        }
        try { s.append(" locals=").append(f.visibleVariables().size()); } catch (AbsentInformationException ex) { s.append(" locals=ABSENT"); }
        if (samples.size() < 3) samples.add(s.toString());
    }

    static void onException(ExceptionEvent xe) {
        try {
            if (xe.catchLocation() == null || !xe.catchLocation().method().equals(xe.location().method())) {
                for (StackFrame f : xe.thread().frames()) {
                    if (match(f.location().method())) { lastException = xe.exception().referenceType().name(); break; }
                }
            }
        } catch (Exception ignored) {}
    }
    static String lastException;

    static void onExit(MethodExitEvent mx) {
        exits++;
        long tid = mx.thread().uniqueID();
        if (inTarget.merge(tid, -1, Integer::sum) <= 0 && excReqs.containsKey(tid)) {
            excReqs.get(tid).disable();
            exitReqs.get(tid).disable();
        }
        Value rv = mx.returnValue();
        if (mx.method().returnTypeName().equals("void") || rv != null || true) returnOk++;
        if (samples.size() < 6) samples.add("exit#" + exits + " returned=" + short1(rv));
    }

    static String render(List<Value> vs) {
        StringBuilder b = new StringBuilder("[");
        for (Value v : vs) b.append(short1(v)).append(", ");
        return b.append("]").toString();
    }

    static String short1(Value v) {
        if (v == null) return "null";
        if (v instanceof StringReference sr) { String x = sr.value(); return "\"" + (x.length() > 60 ? x.substring(0, 60) + "…" : x) + "\""; }
        if (v instanceof PrimitiveValue) return v.toString();
        if (v instanceof ObjectReference o) return o.referenceType().name().replaceAll(".*\\.", "") + "@" + o.uniqueID();
        return v.toString();
    }

    static String esc(String s) { return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " "); }
}
