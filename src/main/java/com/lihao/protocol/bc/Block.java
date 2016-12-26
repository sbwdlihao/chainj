package com.lihao.protocol.bc;

import com.lihao.encoding.blockchain.BlockChain;
import com.lihao.io.WriteTo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

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

    public BlockHeader blockHeader = new BlockHeader();

    public Transaction[] transactions = new Transaction[0];

    public Block() {}

    public Block(BlockHeader blockHeader) {
        this.blockHeader = blockHeader;
    }

    public Block(BlockHeader blockHeader, Transaction[] transactions) {
        this.blockHeader = blockHeader;
        this.transactions = transactions;
    }

    void readFrom(InputStream r) throws IOException {
        int serFlags = blockHeader.readFrom(r);
        if ((serFlags & SerBlockTransactions) == SerBlockTransactions) {
            int n = BlockChain.readVarInt31(r);
            transactions = new Transaction[n];
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
            if (transactions != null) {
                BlockChain.writeVarInt31(w, transactions.length);
                for (Transaction transaction : transactions) {
                    transaction.writeTo(w);
                }
            } else {
                BlockChain.writeVarInt31(w, 0);
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