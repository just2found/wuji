package net.sdvn.nascommon.model.oneos.comp;

import net.sdvn.nascommon.model.oneos.OneOSFile;

import java.util.Comparator;

/**
 * Comparator for File Name
 */
public class OneOSFileSizeComparatorV2 implements Comparator<OneOSFile> {
    private boolean mIsAsc;

    public OneOSFileSizeComparatorV2(boolean isAsc) {
        mIsAsc = isAsc;
    }

    @Override
    public int compare(OneOSFile file1, OneOSFile file2) {
        if (file1 == null || file2 == null) {
            return 0;
        }
        if (file1.isDirectory() && !file2.isDirectory()) {
            return -1;
        }

        if (!file1.isDirectory() && file2.isDirectory()) {
            return 1;
        }
        if (mIsAsc) {
            return Long.compare(file1.getSize(), file2.getSize());
        } else {
            return Long.compare(file2.getSize(), file1.getSize());
        }

    }
}