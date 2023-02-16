package algorithms.RegularSearch;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.LinkedList;

import algorithms.DataStructure.BitArray;
import algorithms.DataStructure.BasicMatrix;

public class RegularSearch {
    public static String FindTT(ArrayList<ArrayList<Integer>> matrixByRows) {
        BasicMatrix mb_simple = new BasicMatrix();
        mb_simple.load(matrixByRows);
        int[] level_desc = mb_simple.regular_sort();

        RegularSearch tt_searcher = new RegularSearch(mb_simple, level_desc);

        long startTime = System.currentTimeMillis();
        LinkedList<LinkedList<Integer>> curr_tt = tt_searcher.search_tt();
        long endTime = System.currentTimeMillis();

        String result = "{'numTT':" + Long.toString(curr_tt.size())
                + ", 'ms_time':" + Long.toString(endTime - startTime)
                + ", 'hits':" + Long.toString(tt_searcher.analyzed_sets) + "}";
        return result;
    }

    BasicMatrix my_mb;
    BitSet[] bit_col_ref;
    int[] lv;
    int analyzed_sets;

    ArrayList<BitArray> bmRowArray;
    ArrayList<BitArray> bmColArray;

    BitArray[][] unitary_rows;
    BitArray[] zero_rows;
    BitArray[] unitary_validator;

    public RegularSearch(BasicMatrix mb, int[] level_desc) {
        my_mb = mb;
        // loadMB(my_mb);
        lv = level_desc;
        bmRowArray = new ArrayList<BitArray>();
        bmColArray = new ArrayList<BitArray>();

        for (int row_pos = 0; row_pos < my_mb.getNumRows(); row_pos++) {
            bmRowArray.add(new BitArray(my_mb.getRowBits(row_pos)));
        }

        for (int col_pos = 0; col_pos < my_mb.getNumFeat(); col_pos++) {
            bmColArray.add(new BitArray(my_mb.getColumnData(col_pos)));
        }

        // init data
        unitary_rows = new BitArray[my_mb.getNumFeat()][my_mb.getNumFeat()];
        zero_rows = new BitArray[my_mb.getNumFeat()];
        unitary_validator = new BitArray[my_mb.getNumFeat()];

        for (int col_a = 0; col_a < my_mb.getNumFeat(); col_a++) {
            zero_rows[col_a] = new BitArray(my_mb.getNumRows());
            unitary_validator[col_a] = new BitArray(my_mb.getNumRows());
            for (int col_b = col_a; col_b < my_mb.getNumFeat(); col_b++) {
                unitary_rows[col_a][col_b] = new BitArray(my_mb.getNumRows());
            }
        }
    }

    private void add_val(int[] candidate, int value) {
        candidate[0] = candidate[0] + 1;
        candidate[candidate[0]] = value;
    }

    private void save_tt(LinkedList<LinkedList<Integer>> tt, int[] candidate) {
        LinkedList<Integer> new_tt = new LinkedList<Integer>();
        for (int row_pos = 1; row_pos < candidate[0] + 1; row_pos++) {
            new_tt.add(candidate[row_pos]);
        }
        tt.add(new_tt);
    }

    private LinkedList<LinkedList<Integer>> search_tt() {
        analyzed_sets = 0;
        LinkedList<LinkedList<Integer>> tt = new LinkedList<LinkedList<Integer>>();

        int stop = my_mb.getNumFeat();

        int[] candidate = new int[my_mb.getNumFeat() + 1];
        for (int col_pos = 0; col_pos < stop; col_pos++) {
            candidate[0] = 0;
            if (!my_mb.getRowBits(0).get(col_pos))
                break;

            add_val(candidate, col_pos);

            if (bmColArray.get(col_pos).cardinality(0) == my_mb.getNumRows()) {
                analyzed_sets++;
                save_tt(tt, candidate);
            } else {
                if (col_pos + 1 != my_mb.getNumFeat()) {
                    unitary_rows[0][0].copyData(bmColArray.get(col_pos));
                    zero_rows[0].copyData(bmColArray.get(col_pos));
                    zero_rows[0].flip();
                    search_regularities(tt, candidate);
                }
            }
        }
        return tt;
    }

    private void search_regularities(LinkedList<LinkedList<Integer>> tt, int[] candidate) {
        analyzed_sets++;
        int level = candidate[0];
        boolean is_regular;
        for (int next_col = candidate[level] + 1; next_col < lv[level]; next_col++) {
        // for (int next_col = candidate[level] + 1; next_col < my_mb.getNumFeat(); next_col++) {
            unitary_rows[level][level].set_to_and(zero_rows[level - 1], bmColArray.get(next_col));
            if (unitary_rows[level][level].cardinality(0) > 0) {
                is_regular = true;
                for (int candidate_pos = 0; candidate_pos < level; candidate_pos++) {
                    unitary_rows[candidate_pos][level].set_to_difference(
                            unitary_rows[candidate_pos][level - 1],
                            bmColArray.get(next_col));

                    if (unitary_rows[candidate_pos][level].cardinality(0) == 0) {
                        is_regular = false;
                        break;
                    }
                }
                if (is_regular) {
                    add_val(candidate, next_col);
                    zero_rows[level].set_to_difference(zero_rows[level - 1], bmColArray.get(next_col));

                    if (zero_rows[level].cardinality(0) == 0)
                        save_tt(tt, candidate);
                    else
                        search_regularities(tt, candidate);

                    candidate[0] = candidate[0] - 1;
                }
            }
        }
    }
}
