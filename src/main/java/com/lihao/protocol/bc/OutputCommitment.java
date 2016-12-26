package com.lihao.protocol.bc;

import com.lihao.encoding.blockchain.BlockChain;

import java.io.*;
import java.util.Arrays;

/**
 * Created by sbwdlihao on 21/12/2016.
 */
public class OutputCommitment {

    public AssetAmount assetAmount = new AssetAmount();

    public long vmVersion;

    public byte[] controlProgram = new byte[0];

    public OutputCommitment() {}

    public OutputCommitment(AssetAmount assetAmount, long vmVersion, byte[] controlProgram) {
        this.assetAmount = assetAmount;
        this.vmVersion = vmVersion;
        this.controlProgram = controlProgram;
    }

    public void readFrom(InputStream r, long txVersion, long assetVersion) throws IOException {
        readFrom(r, txVersion, assetVersion, null);
    }

    public void readFrom(InputStream r, long txVersion, long assetVersion, int[] nOut) throws IOException {
        byte[] b = BlockChain.readVarStr31(r, nOut);
        if (assetVersion != 1) {
            return;
        }
        InputStream in = new ByteArrayInputStream(b);
        int[] n1 = new int[1];
        assetAmount.readFrom(in, n1);
        vmVersion = BlockChain.readVarInt63(in, n1);
        controlProgram = BlockChain.readVarStr31(in, n1);
        if (txVersion == 1 && n1[0] < b.length) {
            throw new IOException("unrecognized extra data in output commitment for transaction version 1");
        }
    }

    public void writeTo(OutputStream w, long assetVersion) throws IOException {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        if (assetVersion == 1) {
            assetAmount.writeTo(buf);
            BlockChain.writeVarInt63(buf, vmVersion);
            BlockChain.writeVarStr31(buf, controlProgram);
        }
        BlockChain.writeVarStr31(w, buf.toByteArray());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OutputCommitment that = (OutputCommitment) o;

        if (vmVersion != that.vmVersion) return false;
        if (assetAmount != null ? !assetAmount.equals(that.assetAmount) : that.assetAmount != null) return false;
        return Arrays.equals(controlProgram, that.controlProgram);
    }

    @Override
    public int hashCode() {
        int result = assetAmount != null ? assetAmount.hashCode() : 0;
        result = 31 * result + (int) (vmVersion ^ (vmVersion >>> 32));
        result = 31 * result + Arrays.hashCode(controlProgram);
        return result;
    }
}
