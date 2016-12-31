package chainj.protocol.bc;

import chainj.crypto.Sha3;
import chainj.protocol.bc.txinput.SpendInput;
import chainj.protocol.state.Output;
import chainj.encoding.blockchain.BlockChain;
import chainj.protocol.bc.txinput.EmptyTxInput;
import chainj.protocol.bc.txinput.IssuanceInput;

import java.io.*;
import java.util.Arrays;
import java.util.Objects;

/**
 * Created by sbwdlihao on 24/12/2016.
 */
public abstract class TxInput {

    protected long assetVersion;

    protected InputCommitment inputCommitment;

    protected byte[] referenceData = new byte[0];

    protected InputWitness inputWitness;

    public long getAssetVersion() {
        return assetVersion;
    }

    public void setAssetVersion(long assetVersion) {
        this.assetVersion = assetVersion;
    }

    public InputCommitment getInputCommitment() {
        return inputCommitment;
    }

    public void setInputCommitment(InputCommitment inputCommitment) {
        Objects.requireNonNull(inputCommitment);
        this.inputCommitment = inputCommitment;
    }

    public byte[] getReferenceData() {
        return referenceData;
    }

    public void setReferenceData(byte[] referenceData) {
        Objects.requireNonNull(referenceData);
        this.referenceData = referenceData;
    }

    public InputWitness getInputWitness() {
        return inputWitness;
    }

    public void setInputWitness(InputWitness inputWitness) {
        Objects.requireNonNull(inputWitness);
        this.inputWitness = inputWitness;
    }

    public static TxInput readFrom(InputStream r, long txVersion) throws IOException {
        TxInput txInput = new EmptyTxInput();
        long assetVersion = BlockChain.readVarInt63(r);
        byte[] inputCommitment = BlockChain.readVarStr31(r);
        if (assetVersion == 1) {
            ByteArrayInputStream icBuf = new ByteArrayInputStream(inputCommitment);
            int icType = icBuf.read();
            if (icType == -1) {
                throw new IOException("read ic type null");
            }
            int bytesRead = 1;
            txInput = createTxInput(icType);
            bytesRead += txInput.inputCommitment.readFrom(icBuf, txVersion);
            if (txVersion == 1 && bytesRead < inputCommitment.length) {
                throw new IOException("unrecognized extra data in input commitment for transaction version 1");
            }
        }
        txInput.setAssetVersion(assetVersion);
        txInput.setReferenceData(BlockChain.readVarStr31(r));
        byte[] inputWitness = BlockChain.readVarStr31(r);
        ByteArrayInputStream iwBuf = new ByteArrayInputStream(inputWitness);
        txInput.inputWitness.readFrom(iwBuf);
        return txInput;
    }

    public void writeTo(OutputStream w, int serFlags) throws IOException {
        BlockChain.writeVarInt63(w, assetVersion);
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        inputCommitment.writeTo(buf);
        BlockChain.writeVarStr31(w, buf.toByteArray());
        BlockChain.writeVarStr31(w, referenceData);
        if ((serFlags & Transaction.SerWitness) != 0) {
            buf.reset();
            inputWitness.writeTo(buf);
            BlockChain.writeVarStr31(w, buf.toByteArray());
        }
    }

    public Output prevOutput() {
        AssetAmount assetAmount = assetAmount();
        TxOutput txOutput = new TxOutput(assetAmount.getAssetID(), assetAmount.getAmount(), controlProgram(), new byte[0]);
        return new Output(outpoint(), txOutput);
    }

    public Hash witnessHash() throws IOException {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        inputWitness.writeTo(buf);
        return new Hash(Sha3.Sum256(buf.toByteArray()));
    }

    protected AssetAmount assetAmount() {
        return new AssetAmount();
    }

    protected byte[] controlProgram() {
        return new byte[0];
    }

    public Outpoint outpoint() {
        return new Outpoint();
    }

    private static TxInput createTxInput(int icType) throws IOException {
        switch (icType) {
            case 0:
                return new IssuanceInput();
            case 1:
                return new SpendInput();
            default:
                throw new IOException("unsupported input type " + icType);
        }
    }

    public boolean isIssuance() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TxInput txInput = (TxInput) o;

        if (assetVersion != txInput.assetVersion) return false;
        if (inputCommitment != null ? !inputCommitment.equals(txInput.inputCommitment) : txInput.inputCommitment != null)
            return false;
        if (!Arrays.equals(referenceData, txInput.referenceData)) return false;
        return inputWitness != null ? inputWitness.equals(txInput.inputWitness) : txInput.inputWitness == null;
    }

    @Override
    public int hashCode() {
        int result = (int) (assetVersion ^ (assetVersion >>> 32));
        result = 31 * result + (inputCommitment != null ? inputCommitment.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(referenceData);
        result = 31 * result + (inputWitness != null ? inputWitness.hashCode() : 0);
        return result;
    }
}
