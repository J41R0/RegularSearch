package algorithms.RegularSearch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import algorithms.DataStructure.BitArray;
import algorithms.DataStructure.BasicMatrix;

class SearchRegularities implements Runnable {

    BasicMatrix my_mb;
    int analyzed_sets;
    LinkedList<LinkedList<Integer>> partial_tt;
    int[] currentCand;

    ArrayList<BitArray> bmColArray;
    int[] lv;

    BitArray[][] unitary_rows;
    BitArray[] zero_rows;
    BitArray[] unitary_validator;

    SearchRegularities(BasicMatrix sortedMB, ArrayList<BitArray> colArray, int[] level_desc, int feat_pos,
            int[] candidate) {
        partial_tt = new LinkedList<LinkedList<Integer>>();
        currentCand = candidate.clone();
        analyzed_sets = 0;
        my_mb = sortedMB;
        bmColArray = colArray;
        lv = level_desc;

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

        unitary_rows[0][0].copyData(bmColArray.get(feat_pos));
        zero_rows[0].copyData(bmColArray.get(feat_pos));
        zero_rows[0].flip();
    }

    public LinkedList<LinkedList<Integer>> getTT() {
        return partial_tt;
    }

    public int getAnalizedSets() {
        return analyzed_sets;
    }

    public static void add_val(int[] candidate, int value) {
        candidate[0] = candidate[0] + 1;
        candidate[candidate[0]] = value;
    }

    public static void save_tt(LinkedList<LinkedList<Integer>> tt, int[] candidate) {
        LinkedList<Integer> new_tt = new LinkedList<Integer>();
        for (int row_pos = 1; row_pos < candidate[0] + 1; row_pos++) {
            new_tt.add(candidate[row_pos]);
        }
        tt.add(new_tt);
    }

    private void search_regularities(LinkedList<LinkedList<Integer>> tt, int[] candidate) {
        analyzed_sets++;
        int level = candidate[0];
        boolean is_regular;
        for (int next_col = candidate[level] + 1; next_col < lv[level]; next_col++) {
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

    @Override
    public void run() {
        search_regularities(partial_tt, currentCand);
    }

}

public class ParallelRS {
    public static String FindTT(ArrayList<ArrayList<Integer>> matrixByRows, boolean sort) {
        BasicMatrix mb_simple = new BasicMatrix();
        mb_simple.load(matrixByRows);
        int[] level_desc;
        if (sort) {
            level_desc = mb_simple.regular_sort();
        } else {
            level_desc = new int[mb_simple.getNumFeat()];
            for (int col_pos = 0; col_pos < mb_simple.getNumFeat(); col_pos++)
                level_desc[col_pos] = mb_simple.getNumFeat();
        }

        ParallelRS tt_searcher = new ParallelRS(mb_simple, level_desc);

        long startTime = System.currentTimeMillis();
        LinkedList<LinkedList<Integer>> curr_tt = tt_searcher.search_tt();
        long endTime = System.currentTimeMillis();

        String result = "{'numTT':" + Long.toString(curr_tt.size())
                + ", 'ms_time':" + Long.toString(endTime - startTime)
                + ", 'hits':" + Long.toString(tt_searcher.analyzed_sets) + "}";
        return result;
    }

    BasicMatrix my_mb;
    int[] lv;
    int analyzed_sets;

    ArrayList<BitArray> bmColArray;

    public ParallelRS(BasicMatrix mb, int[] level_desc) {
        my_mb = mb;
        lv = level_desc;
        bmColArray = new ArrayList<BitArray>();

        for (int col_pos = 0; col_pos < my_mb.getNumFeat(); col_pos++) {
            bmColArray.add(new BitArray(my_mb.getColumnData(col_pos)));
        }
    }

    private LinkedList<LinkedList<Integer>> search_tt() {
        analyzed_sets = 0;
        LinkedList<LinkedList<Integer>> tt = new LinkedList<LinkedList<Integer>>();
        HashMap<Thread, SearchRegularities> regularities = new HashMap<Thread, SearchRegularities>();

        int stop = my_mb.getNumFeat();

        int[] candidate = new int[my_mb.getNumFeat() + 1];
        for (int col_pos = 0; col_pos < stop; col_pos++) {
            candidate[0] = 0;
            if (!my_mb.getRowBits(0).get(col_pos))
                break;

            SearchRegularities.add_val(candidate, col_pos);

            if (bmColArray.get(col_pos).cardinality(0) == my_mb.getNumRows()) {
                analyzed_sets++;
                SearchRegularities.save_tt(tt, candidate);
            } else {
                if (col_pos < my_mb.getNumFeat()) {
                    SearchRegularities subset = new SearchRegularities(my_mb, bmColArray, lv, col_pos, candidate);
                    Thread newT = new Thread(subset);
                    newT.start();
                    regularities.put(newT, subset);
                }
            }
        }

        for (HashMap.Entry<Thread, SearchRegularities> entry : regularities.entrySet()) {
            Thread thread = entry.getKey();
            SearchRegularities subset = entry.getValue();
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            tt.addAll(subset.getTT());
            analyzed_sets += subset.getAnalizedSets();
        }
        return tt;
    }

}
