package algorithms.DataStructure;

import java.util.BitSet;
import java.util.LinkedList;

public class BitArray{

    private int num_bits;
    private int num_blocks;
    private static int BLOCK = 32;
    private int end_mask = 0;

    private int[] block_data_array;
    private int[] block_ones_array;
    private int cardinality_value = 0;

    private int iterator = 0;
    private int value_mark = 0;
    private int block_mark = 0;

    static int[] num_to_bits = new int[] { 0, 1, 1, 2, 1, 2, 2,
                                            3, 1, 2, 2, 3, 2, 3, 3, 4 };


    private int count_bits(int num){
        int nibble = 0;
        if (0 == num)
            return num_to_bits[0];
        // Find last nibble
        nibble = num & 0xf;
        return num_to_bits[nibble] + count_bits(num >>> 4);
    }

    private void set_mask(){
        int last_set_bit = BLOCK - ((BLOCK*num_blocks) - num_bits);
        for (int pos = 0; pos < last_set_bit; ++pos) {
            end_mask |= 1 << pos;
        }
    }

    public BitArray(int bits){
        num_bits = bits;
        num_blocks = (num_bits/BLOCK) + 1;

        block_data_array = new int[num_blocks];
        block_ones_array = new int[num_blocks];

        value_mark = block_data_array[0];
        set_mask();
    }
    
    public BitArray(BitArray other, boolean copy){
        num_bits = other.num_bits;
        num_blocks = other.num_blocks;
        block_data_array = new int[num_blocks];
        block_ones_array = new int[num_blocks];
        if(copy){
            cardinality_value =0;
            for (int curr_block = 0; curr_block < num_blocks; ++curr_block) {
                block_data_array[curr_block] = other.block_data_array[curr_block];
                cardinality_value += count_bits(block_data_array[curr_block]);
                block_ones_array[curr_block] = other.block_ones_array[curr_block];
            }
        }
        value_mark = block_data_array[0];
        set_mask();
    }
    
    public BitArray(LinkedList<Integer> bits){
        num_bits = bits.size();
        num_blocks = (num_bits/BLOCK) + 1;
        block_data_array = new int[num_blocks];
        block_ones_array = new int[num_blocks];

        int curr_block, curr_bit = 0;
        for(Integer value: bits){
            curr_block = curr_bit/BLOCK;
            if(value == 1){
                block_data_array[curr_block] |= 1 << (curr_bit-(curr_block*BLOCK));
                cardinality_value++;
                block_ones_array[curr_block]++;
            }
            curr_bit++;
        }
        value_mark = block_data_array[0];
        set_mask();
    }
    
    public BitArray(BitSet bits){
        num_bits = bits.size();
        num_blocks = (num_bits/BLOCK) + 1;
        block_data_array = new int[num_blocks];
        block_ones_array = new int[num_blocks];

        int curr_block, curr_bit = 0;
        for (int pos = 0; pos < bits.size(); ++pos) {
            curr_block = (curr_bit/BLOCK);
            if(bits.get(pos)){
                block_data_array[curr_block] |= 1 << (curr_bit-(curr_block*BLOCK));
                cardinality_value++;
                block_ones_array[curr_block]++;
            }
            curr_bit++;
        }
        value_mark = block_data_array[0];
        set_mask();
    }
    
    public BitArray and(BitArray other){
        BitArray result = new BitArray(this, false);
        for (int curr_block = 0; curr_block < num_blocks; ++curr_block) {
            result.block_data_array[curr_block] = block_data_array[curr_block] & other.block_data_array[curr_block];
            result.block_ones_array[curr_block] = count_bits(result.block_data_array[curr_block]);
            result.cardinality_value += result.block_ones_array[curr_block];
        }
        result.value_mark = result.block_data_array[0];
        return result;
    }
    
    public void setAnd(BitArray other){
        cardinality_value = 0;
        for (int curr_block = 0; curr_block < num_blocks; ++curr_block) {
            block_data_array[curr_block] &= other.block_data_array[curr_block];
            block_ones_array[curr_block] = count_bits(block_data_array[curr_block]);
            cardinality_value += block_ones_array[curr_block];
        }
        value_mark = block_data_array[block_mark];
    }
    
    public BitArray or(BitArray other){
        BitArray result = new BitArray(this, false);
        for (int curr_block = 0; curr_block < num_blocks; ++curr_block) {
            result.block_data_array[curr_block] = block_data_array[curr_block] | other.block_data_array[curr_block];
            result.block_ones_array[curr_block] = count_bits(result.block_data_array[curr_block]);
            result.cardinality_value += result.block_ones_array[curr_block];
        }
        result.value_mark = result.block_data_array[0];
        return result;
    }
    
    public void setOr(BitArray other){
        cardinality_value = 0;
        for (int curr_block = 0; curr_block < num_blocks; ++curr_block) {
            block_data_array[curr_block] |= other.block_data_array[curr_block];
            block_ones_array[curr_block] = count_bits(block_data_array[curr_block]);
            cardinality_value += block_ones_array[curr_block];
        }
        value_mark = block_data_array[0];
    }
    
    public BitArray difference(BitArray other){
        BitArray result = new BitArray(this, false);
        for (int curr_block = 0; curr_block < num_blocks; ++curr_block) {
            result.block_data_array[curr_block] = block_data_array[curr_block] ^ 
                                    (other.block_data_array[curr_block] & block_data_array[curr_block]);
            block_ones_array[curr_block] = count_bits(block_data_array[curr_block]);
            result.cardinality_value += block_ones_array[curr_block];
        }
        result.value_mark = result.block_data_array[0];
        return result;
    }
    
    public void setDifference(BitArray other){
        cardinality_value = 0;
        for (int curr_block = 0; curr_block < num_blocks; ++curr_block) {
            block_data_array[curr_block] = block_data_array[curr_block] ^ 
                                    (other.block_data_array[curr_block] & block_data_array[curr_block]);
            block_ones_array[curr_block] = count_bits(block_data_array[curr_block]);
            cardinality_value += block_ones_array[curr_block];
        }
        value_mark = block_data_array[0];
    }
    
    public boolean isEqual(BitArray other){
        for (int curr_block = 0; curr_block < num_blocks; ++curr_block) {
            if (block_data_array[curr_block] != other.block_data_array[curr_block])
                return false;
        }
        return true;
    }
    
    public void copyData(BitArray other){
        num_bits = other.num_bits;
        num_blocks = other.num_blocks;
        block_data_array = new int[num_blocks];
        block_ones_array = new int[num_blocks];
        cardinality_value = other.cardinality_value;
        end_mask = other.end_mask;

        iterator = 0;
        block_mark = 0;

        for (int curr_block = 0; curr_block < num_blocks; ++curr_block) {
            block_data_array[curr_block] = other.block_data_array[curr_block];
            block_ones_array[curr_block] = other.block_ones_array[curr_block];
        }

        value_mark = block_data_array[0];
    }
    
    public void set_to_difference(BitArray first, BitArray second){
        cardinality_value = 0;
        for (int curr_block = 0; curr_block < num_blocks; ++curr_block) {
            block_data_array[curr_block] = first.block_data_array[curr_block] ^ 
                            (second.block_data_array[curr_block] & first.block_data_array[curr_block]);
            block_ones_array[curr_block] = count_bits(block_data_array[curr_block]);
            cardinality_value += block_ones_array[curr_block];
        }
        value_mark = block_data_array[0];
    }
    
    public void set_to_and(BitArray first, BitArray second){
        cardinality_value = 0;
        for (int curr_block = 0; curr_block < num_blocks; ++curr_block) {
            block_data_array[curr_block] = second.block_data_array[curr_block] &
                                            first.block_data_array[curr_block];
            block_ones_array[curr_block] = count_bits(block_data_array[curr_block]);
            cardinality_value += block_ones_array[curr_block];
        }
        value_mark = block_data_array[0];
    }
    
    public boolean intersects(BitArray other){
        for (int curr_block = 0; curr_block < num_blocks; ++curr_block) {
            if ((block_data_array[curr_block] & other.block_data_array[curr_block]) > 0)
                return true;
        }
        return false;
    }
    
    public void set(int bit_pos){
        if(bit_pos< num_bits){
            int block = bit_pos/BLOCK;
            block_data_array[block] |= 1 << (bit_pos-(block*BLOCK));
            block_ones_array[block]++;
            cardinality_value++;

            // restart iterator
            reset_iterator();
        }
    }
    
    public void flip(){
        cardinality_value = 0;
        for (int curr_block = 0; curr_block < num_blocks; ++curr_block){
            block_data_array[curr_block] = ~block_data_array[curr_block] ;
            block_ones_array[curr_block] = count_bits(block_data_array[curr_block]);
            cardinality_value += block_ones_array[curr_block];
        }
        cardinality_value = cardinality_value - block_ones_array[num_blocks-1];
        block_data_array[num_blocks-1] = block_data_array[num_blocks-1] & end_mask;
        block_ones_array[num_blocks-1] = count_bits(block_data_array[num_blocks-1]);
        cardinality_value += block_ones_array[num_blocks-1];
        
        value_mark = block_data_array[0];
    }
    
    public boolean iterate(){
        while (value_mark == 0) {
            block_mark++;
            if(block_mark==num_blocks){
                block_mark = 0;
                value_mark = block_data_array[block_mark];
                iterator = 0;
                return false;
            }
            value_mark = block_data_array[block_mark];
            iterator = BLOCK * block_mark;
        }
        while ((value_mark & 1)==0) {
            value_mark = value_mark >>> 1;
            iterator++;
        }
        return true;
    }
    
    public int get_it_bit_pos(){
        int result = iterator;
        value_mark = value_mark >>> 1;
        iterator++;
        return result;
    }
    
    public void reset_iterator(){
        block_mark = 0;
        value_mark = block_data_array[block_mark];
        iterator = 0;
    }
    
    public int cardinality(int bit_pos){
        if (bit_pos == 0)
                return cardinality_value;

        // define bit mask
        int result = 0;
        int curr_mask = 0;
        int last_block = bit_pos/BLOCK;
        int last_bit = BLOCK - ((BLOCK * (last_block + 1)) - bit_pos);
        for (int pos = 0; pos <= last_bit; ++pos)
            curr_mask |= 1 << pos;

        // count bits
        for (int curr_block = 0; curr_block < last_block; ++curr_block)
            result += block_ones_array[curr_block] ;
        result+=count_bits(block_data_array[last_block] & curr_mask);

        return result;
    }
    
    public void clear(){
        cardinality_value = 0;
        for (int curr_block = 0; curr_block < num_blocks; ++curr_block) {
            block_data_array[curr_block] = 0;
            block_ones_array[curr_block] = 0;
        }
        value_mark = block_data_array[0];
    }

    public int get_num_bits(){return num_bits;}
    public int get_num_blocks(){return num_blocks;}
}
    
