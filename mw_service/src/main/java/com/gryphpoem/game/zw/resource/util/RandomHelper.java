package com.gryphpoem.game.zw.resource.util;

import org.apache.commons.lang3.RandomUtils;

import java.util.List;
import java.util.Set;

public class RandomHelper {
	static public boolean isHitRangeIn100(final int prob) {
		final int seed = randomInSize(100);
		boolean bool = false;
		if (seed < prob) {
			bool = true;
		}
		return bool;
	}

	static public boolean isHitRangeIn1000(final int prob) {
		final int seed = randomInSize(1000);
		boolean bool = false;
		if (seed < prob) {
			bool = true;
		}
		return bool;
	}

	static public boolean isHitRangeIn10000(final int prob) {
		final int seed = randomInSize(10000);
		boolean bool = false;
		if (seed < prob) {
			bool = true;
		}
		return bool;
	}

	static public boolean isHitRangeIn100000(final int prob) {
		final int seed = randomInSize(100000);
		boolean bool = false;
		if (seed < prob) {
			bool = true;
		}
		return bool;
	}

	static public int randomInSize(final int size) {
		return RandomUtils.nextInt(0, size);
	}

	static public long randomInSize(final long size) {
		return RandomUtils.nextLong(0, size);
	}

	static public boolean randomBool() {
		return RandomUtils.nextInt(0, 100) % 2 == 0;
	}

	public static int randomInArea(int min, int max) {
		return RandomUtils.nextInt(min, max);
	}

	public static Set<Integer> randomWinCnt(List<Integer> confs, Set<Integer> winCnt) {
		if (!CheckNull.isEmpty(confs)) {
			Integer min = confs.get(0);
			Integer max = confs.get(1);
			Integer cnt = confs.get(2);
			while (true) {
				if (winCnt.size() >= cnt) {
					return winCnt;
				}
				winCnt.add(RandomUtils.nextInt(min, max));
			}
		}
		return winCnt;
	}
}
