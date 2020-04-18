package gophertunnel;

import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

public class AuthChainReturn extends Structure implements Structure.ByValue {
    public String r0; /* auth_chain */
    public String r1; /* err */

    protected List<String> getFieldOrder() {
        return Arrays.asList("r0", "r1");
    }
}
