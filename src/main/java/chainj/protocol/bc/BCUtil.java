package chainj.protocol.bc;

import chainj.encoding.blockchain.BlockChain;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by sbwdlihao on 25/12/2016.
 */
public class BCUtil {

    static int readSerFlags(InputStream r) throws IOException {
        int serFlags = r.read();
        if (serFlags == -1) {
            throw new IOException("read serFlags null");
        }
        return serFlags;
    }

    public static byte[][] readDyadicArray(InputStream r) throws IOException {
        int n = BlockChain.readVarInt31(r);
        byte[][] arguments = new byte[n][];
        for (int i = 0; i < n; i++) {
            arguments[i] = BlockChain.readVarStr31(r);
        }
        return arguments;
    }
    
    public static void writeDyadicArray(ByteArrayOutputStream w, byte[][] arguments) {
        if (arguments != null) {
            BlockChain.writeVarInt31(w, arguments.length);
            for (byte[] argument : arguments) {
                BlockChain.writeVarStr31(w, argument);
            }
        } else {
            BlockChain.writeVarInt31(w, 0);
        }
    }
}
