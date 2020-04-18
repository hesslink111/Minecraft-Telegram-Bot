import com.sun.jna.Pointer;

public class NativeInteropUtils {
  public static byte[] bytesFromPtr(Pointer ptr, int length) {
    return ptr.getByteArray(0, length);
  }
}
