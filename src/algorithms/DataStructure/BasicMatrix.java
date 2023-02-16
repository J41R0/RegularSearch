package algorithms.DataStructure;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.LinkedList;

public class BasicMatrix {
    private LinkedList<BitSet> bit_representation = new LinkedList<BitSet>();
    private ArrayList<ArrayList<Integer>> curr_mb = new ArrayList<ArrayList<Integer>>();
    private int[][] staticBM;
    private int num_feat = 0;
    private int num_row = 0;

    private int hamming_dist(BitSet elem1, BitSet elem2) {
        BitSet result = (BitSet) elem1.clone();
        result.xor(elem2);
        return result.cardinality();
    }

    public void hamming_sort() {
        int[] onesCount = new int[curr_mb.size()];
        for (int row_pos = 0; row_pos < curr_mb.size(); row_pos++) {
            onesCount[row_pos] = bit_representation.get(row_pos).cardinality();
        }
        for (int row_pos = 0; row_pos < curr_mb.size() - 1; row_pos++) {
            int pivot = row_pos + 1;
            int pivot_hamming = hamming_dist(bit_representation.get(row_pos), bit_representation.get(pivot));
            for (int row_pos_1 = row_pos + 1; row_pos_1 < curr_mb.size(); row_pos_1++) {
                int curr_hamming = hamming_dist(bit_representation.get(row_pos), bit_representation.get(pivot));
                if (curr_hamming<pivot_hamming) {
                    swapRows(pivot, row_pos_1);
                }
            }
        }
    }

    private void __reg_sort(int[] level_desc, int level,int row_start, int row_end, int col_start){
        int ones_count;
        int min_ones_pos = -1;
        int min_ones = num_feat;
        for (int row_pos = row_start; row_pos < row_end; row_pos++) {
            ones_count = 0;
            for (int val_pos=col_start; val_pos < num_feat; val_pos++) {
                if(bit_representation.get(row_pos).get(val_pos)){
                    ones_count+=1;
                }
            }
            if (ones_count<min_ones){
                min_ones = ones_count;
                min_ones_pos = row_pos;
            }
        }
        if (min_ones_pos!=-1){
            swapRows(row_start, min_ones_pos);
            int new_col_start = col_start + min_ones;
            level_desc[level] = new_col_start;
            for (int col_pos = col_start; col_pos < new_col_start; col_pos++) {
                if(!bit_representation.get(row_start).get(col_pos)&& col_pos+1<num_feat){
                    ones_count = col_pos+1;
                    for (int val_pos=col_pos+1; val_pos < num_feat; val_pos++) {
                        if (bit_representation.get(row_start).get(val_pos)){
                            ones_count = val_pos;
                            break;
                        }
                    }
                    swapCols(col_pos, ones_count);
                }
            }

            int zeros_count = 0;
            int row_zeros;
            row_start+=1;

            for (int row_pos = row_start; row_pos < row_end; row_pos++) {
                row_zeros = 0;
                for (int val_pos=col_start; val_pos < new_col_start; val_pos++) {
                    if(!bit_representation.get(row_pos).get(val_pos)){
                        row_zeros+=1;
                    }
                }
                if (row_zeros==min_ones){
                    swapRows(row_pos, row_start + zeros_count);
                    zeros_count += 1;
                }
            }
            if (zeros_count > 0)
                __reg_sort(level_desc, level+1, row_start, row_start + zeros_count, new_col_start);
        }
    }

    public int[] regular_sort() {
        int[] level_desc = new int[num_feat];
        for (int col_pos = 0; col_pos < num_feat; col_pos++)
            level_desc[col_pos] = num_feat;
        __reg_sort(level_desc, 0, 0, num_row, 0);
        return level_desc;
    }

    public void load(ArrayList<ArrayList<Integer>> MB) {
        curr_mb = MB;
        num_row = MB.size();
        num_feat = MB.get(0).size();
        staticBM=new int[num_row][num_feat];
        int num_bits = num_feat;

        for (int row_pos = 0; row_pos < MB.size(); row_pos++) {
            BitSet bits = new BitSet(num_feat);
            for (int val_pos = 0; val_pos < num_feat; val_pos++) {
                // staticBM[row_pos][val_pos] = MB.get(row_pos).get(val_pos);
                if (1 == MB.get(row_pos).get(val_pos)) {
                    bits.set(val_pos);
                }
            }
            bit_representation.add(bits);
            // set as first element the lower cardinality value
            if (row_pos == 0) {
                num_bits = bit_representation.get(row_pos).cardinality();
            } else {
                if (num_bits > bit_representation.get(row_pos).cardinality()) {
                    swapRows(0, row_pos);
                }
            }
        }
    }

    public void update_metadata(){
        for (int row_pos = 0; row_pos < curr_mb.size(); row_pos++)
            for (int val_pos = 0; val_pos < num_feat; val_pos++)
                staticBM[row_pos][val_pos] = curr_mb.get(row_pos).get(val_pos);
    }

    public void swapRows(int row_pos1, int row_pos2) {
        ArrayList<Integer> temp_row_list = curr_mb.get(row_pos1);
        curr_mb.set(row_pos1, curr_mb.get(row_pos2));
        curr_mb.set(row_pos2, temp_row_list);

        BitSet temp_row_bits = bit_representation.get(row_pos1);
        bit_representation.set(row_pos1, bit_representation.get(row_pos2));
        bit_representation.set(row_pos2, temp_row_bits);
    }

    public void swapCols(int col_pos1, int col_pos2) {
        for(int row = 0; row < curr_mb.size(); row++){
            boolean temp_val = bit_representation.get(row).get(col_pos1);
            bit_representation.get(row).set(col_pos1, bit_representation.get(row).get(col_pos2));
            bit_representation.get(row).set(col_pos2, temp_val);

            int temp_int_val = curr_mb.get(row).get(col_pos1);
            curr_mb.get(row).set(col_pos1, curr_mb.get(row).get(col_pos2));
            curr_mb.get(row).set(col_pos2, temp_int_val);
        }
    }

    public int getNumRows() {
        return num_row;
    }

    public int getNumFeat() {
        return num_feat;
    }

    public BitSet getRowBits(Integer row_pos) {
        return bit_representation.get(row_pos);
    }

    public LinkedList<Integer> getColumnData(int col_pos){
        LinkedList<Integer> result = new LinkedList<Integer>();
        for (int row_pos = 0; row_pos < curr_mb.size(); row_pos++) {
            result.add(curr_mb.get(row_pos).get(col_pos));
        }
        return result;
    }
    public int getValue(int row, int col){
        return staticBM[row][col];
    }

    public void print(){
        // String line = "";
        for(int row = 0; row < curr_mb.size(); row++){
            System.out.println(curr_mb.get(row));
        }
    }

}
