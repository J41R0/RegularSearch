package algorithms.YYC;

import java.util.ArrayList;
import java.util.BitSet;

import algorithms.DataStructure.BitArray;
import algorithms.DataStructure.BasicMatrix;

public class YYC {
	public static String FindTT(ArrayList<ArrayList<Integer>> matrixByRows) {
		BasicMatrix mb_simple = new BasicMatrix();
		mb_simple.load(matrixByRows);
		mb_simple.hamming_sort();

		YYC yycAlgorithm = new YYC(mb_simple);

		long startTime = System.currentTimeMillis();
		ArrayList<BitSet> tt_list = yycAlgorithm.search_tt();
		long endTime = System.currentTimeMillis();

		int num_tt = tt_list.size();
		String result = "{'numTT':" + Long.toString(num_tt)
				+ ", 'ms_time':" + Long.toString(endTime - startTime)
				+ ", 'hits':" + Long.toString(yycAlgorithm.candidates) + "}";
		return result;
	}

	public int candidates;
	public int num_tt;
	BasicMatrix BM;

	ArrayList<BitArray> bmColArray;
	ArrayList<BitArray> bmRowArray;

	// find compatible set by rows vars
	BitArray condition_validator;
	BitArray evaluator;

	BitArray[][] unitary_rows;
	BitArray[] zero_rows;
	BitArray inf_compatibility;

	public YYC(BasicMatrix someBM) {
		BM = someBM;
		bmRowArray = new ArrayList<BitArray>();
		bmColArray = new ArrayList<BitArray>();

		for (int row_pos = 0; row_pos < BM.getNumRows(); row_pos++) {
			bmRowArray.add(new BitArray(BM.getRowBits(row_pos)));
		}

		for (int col_pos = 0; col_pos < BM.getNumFeat(); col_pos++) {
			bmColArray.add(new BitArray(BM.getColumnData(col_pos)));
		}
		num_tt = 0;

		// init data
		condition_validator = new BitArray(bmRowArray.get(0), false);
		evaluator = new BitArray(bmRowArray.get(0), false);

		unitary_rows = new BitArray[BM.getNumFeat()][BM.getNumFeat()];
		zero_rows = new BitArray[BM.getNumFeat()];

		for (int col_a = 0; col_a < BM.getNumFeat(); col_a++) {
			zero_rows[col_a] = new BitArray(BM.getNumRows());
			// unitary_rows[col_a] = new BitArray [BM.getNumFeat()];
			for (int col_b = col_a; col_b < BM.getNumFeat(); col_b++) {
				unitary_rows[col_a][col_b] = new BitArray(BM.getNumRows());
			}
		}
		inf_compatibility = new BitArray(BM.getNumRows());
	}

	private ArrayList<BitSet> search_tt() {
		candidates = 0;
		ArrayList<BitSet> tt_set = new ArrayList<BitSet>();
		if (BM.getNumRows() == 0)
			return tt_set;

		// process the first row
		BitSet first_row = BM.getRowBits(0);
		for (int feat_pos = 0; feat_pos < BM.getNumFeat(); feat_pos++) {
			if (first_row.get(feat_pos)) {
				BitSet new_testor = new BitSet(BM.getNumFeat());
				new_testor.set(feat_pos);
				tt_set.add(new_testor);
			}
		}

		// for (int row_pos = 1; row_pos <= BM.getNumRows(); row_pos++) {
		int row_pos = 1;
		while (row_pos < BM.getNumRows()) {
			// System.out.println(tt_set);
			BitSet current_row_bits = BM.getRowBits(row_pos);
			// System.out.println(current_row_bits);
			ArrayList<BitSet> tt_aux = new ArrayList<BitSet>();

			for (int tt_pos = 0; tt_pos < tt_set.size(); tt_pos++) {
				BitSet curr_tt = tt_set.get(tt_pos);
				if (curr_tt.intersects(current_row_bits)) {
					tt_aux.add(curr_tt);
				} else {
					for (int feat_pos = 0; feat_pos < BM.getNumFeat(); feat_pos++) {
						if (current_row_bits.get(feat_pos) && !curr_tt.get(feat_pos)) {
							candidates++;
							if (find_compatible_sets(curr_tt, feat_pos, row_pos + 1, BM)) {
								BitSet new_tt = (BitSet) curr_tt.clone();
								new_tt.set(feat_pos);
								tt_aux.add(new_tt);
							}
						}
					}
				}
			}
			tt_set = tt_aux;
			row_pos += 1;
		}
		num_tt = tt_set.size();
		return tt_set;
	}

	private boolean find_compatible_sets(BitSet curr_tt, int feat_pos, int row_index, BasicMatrix MB) {
		BitSet possible_tt = (BitSet) curr_tt.clone();
		possible_tt.set(feat_pos);
		int unitary_count = 0;
		BitSet conddition_validator = new BitSet(MB.getNumFeat());
		for (int row_pos = 0; row_pos < row_index; row_pos++) {
			BitSet evaluator = (BitSet) possible_tt.clone();
			evaluator.and(MB.getRowBits(row_pos));

			if (1 == evaluator.cardinality()) {
				unitary_count += 1;
				conddition_validator.or(evaluator);
			}
		}
		// condition 1
		if (unitary_count < possible_tt.cardinality())
			return false;
		// condition 2
		if (!conddition_validator.equals(possible_tt))
			return false;

		return true;
	}
}
