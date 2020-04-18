package gophertunnel;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

public class GoSlice extends Structure implements Structure.ByValue {
    public Pointer data;
    public long len;
    public long cap;

    protected List<String> getFieldOrder() {
        return Arrays.asList("data", "len", "cap");
    }

    public static GoSlice fromBytes(byte[] bytes) {
        var goSlice = new GoSlice();
        goSlice.data = new Memory(bytes.length);
        goSlice.data.write(0, bytes, 0, bytes.length);
        goSlice.cap = bytes.length;
        goSlice.len = bytes.length;
        return goSlice;
    }
}