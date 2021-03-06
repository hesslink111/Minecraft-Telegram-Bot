package gophertunnel;

import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

public class GoString extends Structure implements Structure.ByValue {
    public String p;
    public long n;

    public GoString() {
    }

    public GoString(String content) {
        this.p = content;
        this.n = p.length();
    }

    public static GoString from(String content) {
        return new GoString(content);
    }

    protected List<String> getFieldOrder() {
        return Arrays.asList("p", "n");
    }

    @Override
    public String toString() {
        return p;
    }
}
