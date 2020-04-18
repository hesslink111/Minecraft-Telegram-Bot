package gophertunnel;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

public class EncodeLoginPacketReturn extends Structure implements Structure.ByValue {
    public Pointer r0; /* login_packet_bytes */
    public int r1;  // login_packet_bytes_len
    public String r2; // XUID
    public String r3; // Identity
    public String r4; /* err */

    protected List<String> getFieldOrder() {
        return Arrays.asList("r0", "r1", "r2", "r3", "r4");
    }
}
