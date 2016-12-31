package chainj.protocol.bc;

import chainj.encoding.blockchain.BlockChain;
import chainj.io.WriteTo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Objects;

/**
 * Created by sbwdlihao on 26/12/2016.
 */
public class Block implements WriteTo{

    static final int SerBlockWitness = 1;
    private static final int SerBlockTransactions = 2;
    static final int SerBlockSigHash = 0;
    static final int SerBlockHeader = SerBlockWitness;
    static final int SerBlockFull = SerBlockWitness | SerBlockTransactions;

    // NewBlockVersion is the version to use when creating new blocks.
    static final int NewBlockVersion = 1;

    private BlockHeader blockHeader = new BlockHeader();

    private Transaction[] transactions = new Transaction[0];

    public BlockHeader getBlockHeader() {
        return blockHeader;
    }

    public void setBlockHeader(BlockHeader blockHeader) {
        Objects.requireNonNull(blockHeader);
        this.blockHeader = blockHeader;
    }

    public Transaction[] getTransactions() {
        return transactions;
    }

    public void setTransactions(Transaction[] transactions) {
        Objects.requireNonNull(blockHeader);
        this.transactions = transactions;
    }

    public long getHeight() {
        return blockHeader.getHeight();
    }

    public Hash getHash() throws IOException {
        return blockHeader.hash();
    }

    public Block() {}

    public Block(BlockHeader blockHeader) {
        setBlockHeader(blockHeader);
    }

    public Block(BlockHeader blockHeader, Transaction[] transactions) {
        setBlockHeader(blockHeader);
        setTransactions(transactions);
    }

    void readFrom(InputStream r) throws IOException {
        int serFlags = blockHeader.readFrom(r);
        if ((serFlags & SerBlockTransactions) == SerBlockTransactions) {
            int n = BlockChain.readVarInt31(r);
            setTransactions(new Transaction[n]);
            for (int i = 0; i < n; i++) {
                TxData txData = new TxData();
                txData.readFrom(r);
                transactions[i] = new Transaction(txData);
            }
        }
    }

    public void writeTo(OutputStream w) throws IOException {
        writeTo(w, SerBlockFull);
    }

    void writeTo(OutputStream w, int serFlags) throws IOException {
        blockHeader.writeTo(w, serFlags);
        if ((serFlags & SerBlockTransactions) == SerBlockTransactions) {
            BlockChain.writeVarInt31(w, transactions.length);
            for (Transaction transaction : transactions) {
                transaction.writeTo(w);
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Block block = (Block) o;

        if (blockHeader != null ? !blockHeader.equals(block.blockHeader) : block.blockHeader != null) return false;
        return Arrays.equals(transactions, block.transactions);
    }

    @Override
    public int hashCode() {
        int result = blockHeader != null ? blockHeader.hashCode() : 0;
        result = 31 * result + Arrays.hashCode(transactions);
        return result;
    }
}