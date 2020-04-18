package gophertunnel;

import com.sun.jna.Library;
import com.sun.jna.Native;

import java.io.File;

public interface Gophertunnel extends Library {
  String LIB_PATH = new File("go/gophertunnel_interop.so").getAbsolutePath();
  Gophertunnel GOPHERTUNNEL = Native.load(LIB_PATH, Gophertunnel.class);

  AuthChainReturn AuthChain(GoString email, GoString password, GoSlice privateKey);

  EncodeLoginPacketReturn EncodeLoginPacket(
      GoString chainData,
      GoString address,
      GoSlice privateKey,
      int currentProtocol,
      GoString currentVersion);
}
